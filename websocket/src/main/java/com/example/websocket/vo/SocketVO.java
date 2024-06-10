package com.example.websocket.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocketVO {
    private String userName;    // 유저명
    private String content;     // 메시지 내용
    private Object canvasObj;   // 그리기 내용
}