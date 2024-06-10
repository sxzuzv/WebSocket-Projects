package com.example.websocket.controller;

import com.example.websocket.vo.SocketVO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @MessageMapping("/receive")
    @SendTo("/send")
    public SocketVO SocketHandler(SocketVO socketVO) {
        String userName = socketVO.getUserName();
        String content = socketVO.getContent();
        Object canvasObj = socketVO.getCanvasObj();

        SocketVO result = new SocketVO(userName, content, canvasObj);

        return result;
    }
}