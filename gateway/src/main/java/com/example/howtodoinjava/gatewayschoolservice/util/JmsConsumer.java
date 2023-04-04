package com.example.howtodoinjava.gatewayschoolservice.util;

import com.example.howtodoinjava.gatewayschoolservice.controller.GatewayController;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;

@Component
@EnableJms
public class JmsConsumer implements MessageListener {

    @Autowired
    GatewayController gatewayController;
    @Override
    @JmsListener(destination = "${active-mq.school-resp-topic}")
    public void onMessage(Message message) {
        try{

            String text = ((ActiveMQTextMessage) message).getText();
            //do additional processing
            System.out.println("Received Message from Topic: "+ text);
            gatewayController.setSchoolResp(text);
            gatewayController.getCountDownLatch().countDown();


        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}