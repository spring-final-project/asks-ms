package com.springcloud.demo.asksmicroservice.messaging;

public interface MessagingProducer {
    void sendMessage(String topic, String message);
}
