package com.epam.sqs.handler;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.epam.sqs.dto.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderSenderHandler implements RequestHandler<Order, Void> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Void handleRequest(Order order, Context context) {
        LambdaLogger log = context.getLogger();
        log.log("Received an order: " + order);

        if (order.getType() == null || order.getTotal() == null)
            throw new RuntimeException("Provide more information!");

        if (order.getType().equals(Order.Type.COUNTABLE) && order.getVolume() != null)
            throw new RuntimeException("Countable type cannot have volume value!");

        if (order.getType().equals(Order.Type.LIQUID) && order.getQuantity() != null)
            throw new RuntimeException("Liquid type cannot have quantity value!");

        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        String orderQueueURL = sqs.getQueueUrl("orders").getQueueUrl();

        String messageBody = null;
        try {
            messageBody = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            log.log(e.getMessage());

            return null;
        }

        SendMessageRequest orderMessage = new SendMessageRequest()
                .withQueueUrl(orderQueueURL)
                .withMessageBody(messageBody)
                .withDelaySeconds(5);

        sqs.sendMessage(orderMessage);

        log.log("Message was sent! Congrats!");

        return null;
    }
}
