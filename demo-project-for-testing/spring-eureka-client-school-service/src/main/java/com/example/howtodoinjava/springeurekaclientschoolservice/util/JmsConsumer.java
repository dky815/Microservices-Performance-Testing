package com.example.howtodoinjava.springeurekaclientschoolservice.util;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
@EnableJms
public class JmsConsumer implements MessageListener {


    @Autowired
    JmsProducer jmsProducer;
    @Override
    @JmsListener(destination = "${active-mq.school-req-topic}")
    public void onMessage(Message message) {
        try{

            String text = ((ActiveMQTextMessage) message).getText();
            //do additional processing
            System.out.println("Received Message from Topic: "+ text);
            jmsProducer.sendMessage("8888 University Dr W, Burnaby, BC V5A 1S6", "school.resp.topic");
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}