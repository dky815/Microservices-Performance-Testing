package com.example.howtodoinjava.gatewayschoolservice.controller;

import com.example.howtodoinjava.gatewayschoolservice.util.JmsProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;

@RestController
public class GatewayController {
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	JmsProducer jmsProducer;

	String studentResp;

	String schoolResp;

	CountDownLatch countDownLatch=new CountDownLatch(2);

	@RequestMapping(value = "/gateway/{schoolname}", method = RequestMethod.GET)
	public String getStudents(@PathVariable String schoolname) {
		System.out.println("Getting School details for " + schoolname);
		String studentResponse = restTemplate.exchange("http://student-service/getStudentDetailsForSchool/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
		}, schoolname).getBody();
		String schoolResponse = restTemplate.exchange("http://school-service/getSchoolDetails/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
		}, schoolname).getBody();
		System.out.println("studentResponse Received as " + studentResponse);

		return "School Info -  " + schoolResponse + " \n Student Details " + studentResponse;
	}


	@RequestMapping(value = "/gatewayQueue/{schoolname}", method = RequestMethod.GET)
	public String getStudentsQueue(@PathVariable String schoolname) {
		try {


			System.out.println("Getting School details for " + schoolname);


			jmsProducer.sendMessage(schoolname, "school.req.topic");
			jmsProducer.sendMessage(schoolname, "student.req.topic");
			countDownLatch.await();
			System.out.println("studentResponseQueue Received as " + studentResp);
		}catch (Exception e){
			e.printStackTrace();
		}

		return "School Info -  " + schoolResp + " \n Student Details " + studentResp;

	}


	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public void setStudentResp(String studentResp) {
		this.studentResp = studentResp;
	}

	public void setSchoolResp(String schoolResp) {
		this.schoolResp = schoolResp;
	}

	public CountDownLatch getCountDownLatch(){
		return this.countDownLatch;
	}


}
