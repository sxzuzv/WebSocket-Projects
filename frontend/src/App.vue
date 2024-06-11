<template>
  <div id="app">
    <div class="fabric">
      <canvas ref="canvasRef" width="300" height="300"/>
      <button @click="drawLine">선</button>
      <button @click="drawSquare">사각형</button>
      <button @click="clearCanvas">지우기</button>
    </div>
  </div>
</template>

<script>
import Stomp from 'webstomp-client'
import SockJS from 'sockjs-client'
import {fabric} from "fabric"

export default {
  name: 'App',
  mounted() {
    const canvas = new fabric.Canvas(this.$refs.canvasRef, {  // eslint-disable-line no-unused-vars
      height: 500,
      width: 500
    });

    this.canvas = canvas;

    // 캔버스에 그림이 그려질 때마다 호출되는 이벤트 핸들러
    canvas.on("mouse:up", () => {
      // 그림이 그려질 때마다 객체 출력
      this.printCanvasObjects();
    });
  },

  data() {  // 데이터 정의
    return {
      // userName: "",
      // message: "",
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
    // 캔버스에 그려진 그림 객체를 가져와서 콘솔에 출력하는 함수
    printCanvasObjects () {
      // Fabric.js에서 제공하는 canvas.toObject(): 캔버스에 그려진 객체들의 상태를 JSON 형식으로 가져온다.
      // let canvasObj = JSON.stringify(canvas.toDatalessJSON());
      let canvasObj = this.canvas.toDataURL({
        format: 'png',
        quality: 1.0
      });
      console.log("캔버스에 그려진 객체(Base64):", canvasObj);

      this.send(canvasObj);
    },

    drawLine() {
      this.canvas.isDrawingMode = true;
    },

    // 도형(사각형) 그리기
    drawSquare() {
      this.canvas.isDrawingMode = false;

      const rect = new fabric.Rect({
        top: 100,
        left: 100,
        width: 50,
        height: 50,
        fill: "#f3f345"
      });

      this.canvas.add(rect);
      this.printCanvasObjects();
    },

    // 전송받은 캔버스 데이터 출력
    drawReceivedCanvasObj(canvasObj) {
      // canvasObj를 base64에서 canvas 출력 데이터로 변환
      const image = new Image();
      image.onload = () => {
        const imgInstance = new fabric.Image(image);
        this.canvas.add(imgInstance);
        this.canvas.renderAll();
      };

      image.src = canvasObj;
    },

    // '지우기' 상태 전송
    sendClear() {
      if (this.stompClient && this.stompClient.connected) {
        const msg = {
          action: 'clear'
        };
        this.stompClient.send("/receive", JSON.stringify(msg), {});
      }
    },

    // 캔버스 지우기
    clearCanvas() {
      this.canvas.clear();
      this.sendClear();
    },

    send(canvasObj) {
      if (this.stompClient && this.stompClient.connected) {
        const msg = {
          // VO의 필드명과 동일하게 지정 필요
          // userName: this.userName,
          // content: this.message,
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
              console.log('구독으로 받은 메시지입니다.', res.body);

              const receiveMsg = JSON.parse(res.body);

              if (receiveMsg.action === 'clear') {
                console.log("캔버스 지우기 실행");
                this.canvas.clear();
              } else if (receiveMsg.canvasObj) {
                this.drawReceivedCanvasObj(receiveMsg.canvasObj);
              }
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