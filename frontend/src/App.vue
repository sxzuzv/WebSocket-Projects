<template>
  <div id="app">
    이름: <input
        v-model="userName"
        type="text"
        @input="sendMessage">
    내용: <input
        v-model="message"
        type="text"
        @input="sendMessage">
    <div
        v-for="(item, idx) in recvList"
        :key="idx">
      <h3>이름: {{ item.userName }}</h3>
      <h3>내용: {{ item.content }}</h3>
    </div>
    <div class="fabric">
      <canvas ref="canvasRef" width="300" height="300"/>
      <button @click="drawSquare">사각형</button>
      <button @click="clearCanvas">지우기</button>
    </div>
  </div>
</template>

<script>
import Stomp from 'webstomp-client'
import SockJS from 'sockjs-client'
import {fabric} from "fabric"
// const canvas = null

export default {
  name: 'App',
  mounted() {
    const canvas = new fabric.Canvas(this.$refs.canvasRef, {  // eslint-disable-line no-unused-vars
      isDrawingMode: true // false 설정 시, 그리기 불가
    });

    this.canvas = canvas;

    // 캔버스에 그려진 그림 객체를 가져와서 콘솔에 출력하는 함수
    const printCanvasObjects = () => {
      // Fabric.js에서 제공하는 canvas.toObject(): 캔버스에 그려진 객체들의 상태를 JSON 형식으로 가져온다.
      // let canvasObj = JSON.stringify(canvas.toDatalessJSON());
      let canvasObj = canvas.toDataURL({
        format: 'png',
        quality: 1.0
      });
      console.log("캔버스에 그려진 객체(Base64):", canvasObj);

      this.send(canvasObj);
    };

    // 캔버스에 그림이 그려질 때마다 호출되는 이벤트 핸들러
    canvas.on("mouse:up", () => {
      // 그림이 그려질 때마다 객체 출력
      printCanvasObjects();
    });
  },
  data() {  // 데이터 정의
    return {
      userName: "",
      message: "",
      recvList: [],  // 실시간으로 수신된 메시지를 추가할 리스트
      stompClient: null,
      canvasObj: []
    };
  },
  created() {
    // App.vue가 생성되면 소켓 연결을 시도합니다.
    this.connect();
  },
  methods: {
    // 도형(사각형) 그리기
    drawSquare() {
      const rect = new fabric.Rect({
        top: 100,
        left: 100,
        width: 50,
        height: 50,
        fill: "#f3f345"
      });
      this.canvas.add(rect)
    },
    // 캔버스에 그림을 그리는 메서드
    drawReceivedCanvasObj(canvasObj) {
      // canvasObj를 JSON 문자열에서 JavaScript 객체로 변환
      // const parsedCanvasObj = JSON.parse(canvasObj);

      // canvasObj를 base64에서 canvas 출력 데이터로 변환
      const image = new Image();
      image.onload = () => {
        // Create a fabric image instance from the loaded image
        const imgInstance = new fabric.Image(image);
        // Add the image to the canvas
        this.canvas.add(imgInstance);
        this.canvas.renderAll();
      };

      image.src = canvasObj;

      // Fabric.js의 loadFromJSON 메서드를 사용하여 캔버스에 그림을 로드합니다.
      // this.canvas.loadFromJSON(parsedCanvasObj, () => {
      //   // 그림이 로드된 후에 필요한 작업을 수행할 수 있습니다.
      //   this.canvas.renderAll();
      // });
    },
    clearCanvas() {
      this.canvas.clear(); // 캔버스를 지웁니다.
    },
    sendMessage() {
      if (this.userName !== '' || this.message !== '') {
          console.log("전송 메시지 : ", this.message);
          this.send();
      }
    },
    send(canvasObj) {
      if (this.stompClient && this.stompClient.connected) {
        const msg = {
          // VO의 필드명과 동일하게 지정 필요
          userName: this.userName,
          content: this.message,
          canvasObj: canvasObj
        };
        this.stompClient.send("/receive", JSON.stringify(msg), {});
      }
    },
    connect() {
      const serverURL = "http://localhost:8080"
      let socket = new SockJS(serverURL);
      this.stompClient = Stomp.over(socket);
      console.log(`소켓 연결을 시도합니다. 서버 주소: ${serverURL}`)
      this.stompClient.connect(
          {},
          frame => {
            // 소켓 연결 성공
            this.connected = true;
            console.log('소켓 연결 성공', frame);
            // 서버의 메시지 전송 endpoint를 구독합니다.
            // 이런형태를 pub sub 구조라고 합니다.
            this.stompClient.subscribe("/send", res => {
              console.log('구독으로 받은 메시지 입니다.', res.body);

              // 받은 데이터를 json으로 파싱하고 리스트에 넣어줍니다.
              this.recvList.push(JSON.parse(res.body));
              console.log("recvList 내용", this.recvList);

              // 캔버스에 받은 데이터를 그립니다.
              this.drawReceivedCanvasObj(this.recvList[this.recvList.length - 1].canvasObj);
            });
          },
          error => {
            // 소켓 연결 실패
            console.log('소켓 연결 실패', error);
            this.connected = false;
          }
      );
    }
  }
}
</script>

<style>
  .fabric {
    height: 300px;
    display: inline;
    align-items: center;
    justify-content: center;
  }

  canvas {
    border: 1px solid #42b983;
    border-radius: 8px;
  }
</style>