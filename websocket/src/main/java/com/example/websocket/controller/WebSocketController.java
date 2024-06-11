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
        Object canvasObj = socketVO.getCanvasObj();
        String action = socketVO.getAction();

        SocketVO result = new SocketVO(canvasObj, action);

        return result;
    }
}