package com.example.howtodoinjava.gatewayschoolservice.controller;

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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.Message;
import java.util.List;

@RestController
public class GatewayController {
	@Autowired
	RestTemplate restTemplate;

	@RequestMapping(value = "/gateway/{schoolname}", method = RequestMethod.GET)
	public String getStudents(@PathVariable String schoolname) {
		System.out.println("Getting School details for " + schoolname);

		String accessKey = "your-access-key";
		String secretKey = "your-secret-key";
		String queueUrl = "https://sqs.us-east-1.amazonaws.com/944971585936/test";

		String message = schoolname;

		//send
		AmazonSQS sqs = AmazonSQSClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(queueUrl, "us-east-1"))
				.withCredentials(new AWSStaticCredentialsProvider(
						new BasicAWSCredentials(accessKey, secretKey)))
				.build();

		SendMessageRequest send_msg_request = new SendMessageRequest()
				.withQueueUrl(queueUrl)
				.withMessageBody(message)
				.withDelaySeconds(5); // Optional delay before the message is available to consumers

		sqs.sendMessage(send_msg_request);

		//receive
		while (true) {
			ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
					.withQueueUrl(queueUrl) // Replace with your queue URL
					.withMaxNumberOfMessages(1) // Max messages to receive per request (1-10)
					.withWaitTimeSeconds(20); // Long polling wait time (0-20 seconds)

			ReceiveMessageResult result = sqs.receiveMessage(receiveRequest);
			List<Message> messages = result.getMessages();

			for (Message message : messages) {
				// Process the message here
				System.out.println("Received message: " + message.getBody());

				// Delete the message from the queue once it's processed
				String messageReceiptHandle = message.getReceiptHandle();
				sqs.deleteMessage(queueUrl, messageReceiptHandle);
			}
		}



		String studentResponse = restTemplate.exchange("http://student-service/getStudentDetailsForSchool/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
		}, schoolname).getBody();
		String schoolResponse = restTemplate.exchange("http://school-service/getSchoolDetails/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
		}, schoolname).getBody();
		System.out.println("studentResponse Received as " + studentResponse);

		return "School Info -  " + schoolResponse + " \n Student Details " + studentResponse;
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
