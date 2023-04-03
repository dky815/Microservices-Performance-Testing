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

import java.util.List;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@RestController
public class GatewayController {
	@Autowired
	RestTemplate restTemplate;

	@RequestMapping(value = "/gateway/{schoolname}", method = RequestMethod.GET)
	public String getStudents(@PathVariable String schoolname) {
		System.out.println("Getting School details for " + schoolname);

		String accessKey = "accessKey"; // replace with AKIA****
		String secretKey = "secretKey"; // replace with FgDj****
		String queueUrl = "https://sqs.us-east-1.amazonaws.com/944971585936/test";

		String message = schoolname;

		SqsClient sqsClient = SqsClient.builder()
				.region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
				.build();

		//send
		System.out.println("\nSend messages");
		try {
			// snippet-start:[sqs.java2.sqs_example.send_message]
			sqsClient.sendMessage(SendMessageRequest.builder()
					.queueUrl(queueUrl)
					.messageBody(schoolname)
					.build());
			// snippet-end:[sqs.java2.sqs_example.send_message]

		} catch (SqsException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}

		//receive
		System.out.println("\nReceive messages");
		try {
			// snippet-start:[sqs.java2.sqs_example.retrieve_messages]
			ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
					.queueUrl(queueUrl)
					.maxNumberOfMessages(1)
					.build();
			List<Message> message1 = sqsClient.receiveMessage(receiveMessageRequest).messages();
			for (Message m : message1) {
				schoolname = m.body();

				System.out.println("\n" + m.body());

				String studentResponse = restTemplate.exchange("http://student-service/getStudentDetailsForSchool/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
				}, schoolname).getBody();
				String schoolResponse = restTemplate.exchange("http://school-service/getSchoolDetails/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
				}, schoolname).getBody();
				System.out.println("studentResponse Received as " + studentResponse);

				DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
						.queueUrl(queueUrl)
						.receiptHandle(m.receiptHandle())
						.build();
				sqsClient.deleteMessage(deleteMessageRequest);

				return "School Info -  " + schoolResponse + " \n Student Details " + studentResponse;
			}

		} catch (SqsException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
//		String studentResponse = restTemplate.exchange("http://student-service/getStudentDetailsForSchool/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
//		}, schoolname).getBody();
//		String schoolResponse = restTemplate.exchange("http://school-service/getSchoolDetails/{schoolname}", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
//		}, schoolname).getBody();
//		System.out.println("studentResponse Received as " + studentResponse);
//		return "School Info -  " + schoolResponse + " \n Student Details " + studentResponse;
		return null;
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
