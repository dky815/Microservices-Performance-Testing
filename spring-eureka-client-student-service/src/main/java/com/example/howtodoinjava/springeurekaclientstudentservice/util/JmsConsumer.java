package com.example.howtodoinjava.springeurekaclientstudentservice.util;

import com.alibaba.fastjson2.JSON;
import com.example.howtodoinjava.springeurekaclientstudentservice.domain.Student;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@EnableJms
public class JmsConsumer implements MessageListener {

    private static Map<String, List<Student>> schooDB;
    static {
        schooDB = new HashMap<String, List<Student>>();

        List<Student> lst = new ArrayList<Student>();
        Student std = new Student("Sajal", "Class IV");
        lst.add(std);
        std = new Student("Lokesh", "Class V");
        lst.add(std);

        schooDB.put("abcschool", lst);

        lst = new ArrayList<Student>();
        std = new Student("Kajal", "Class III");
        lst.add(std);
        std = new Student("Sukesh", "Class VI");
        lst.add(std);

        schooDB.put("xyzschool", lst);

    }
    @Autowired
    JmsProducer jmsProducer;
    @Override
    @JmsListener(destination = "${active-mq.student-req-topic}")
    public void onMessage(Message message) {
        try{

            String schoolname = ((ActiveMQTextMessage) message).getText();
            //do additional processing

            List<Student> studentList = schooDB.get(schoolname);
            if (studentList == null) {
                studentList = new ArrayList<Student>();
                Student std = new Student("Not Found", "N/A");
                studentList.add(std);
            }
            jmsProducer.sendMessage(JSON.toJSONString(studentList),"student.resp.topic");
            System.out.println("Received Message from Topic: "+ schoolname);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}