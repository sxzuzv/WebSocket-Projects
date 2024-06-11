//package com.example.websocket.service;
//
//import org.apache.kafka.common.protocol.Message;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class Sender {
//    private final KafkaTemplate<String, Message> kafkaTemplate;
//
//    public Sender(KafkaTemplate<String, Message> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    public void send(String topic, Message message) {
//        kafkaTemplate.send(topic, message);
//    }
//}