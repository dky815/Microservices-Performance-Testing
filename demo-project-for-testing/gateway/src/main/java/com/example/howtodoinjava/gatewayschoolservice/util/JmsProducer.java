package com.example.howtodoinjava.gatewayschoolservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component

public class JmsProducer {

    @Autowired
    JmsTemplate jmsTemplate;


    public void sendMessage(String message, String topic){
        try{

            jmsTemplate.convertAndSend(topic, message);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
