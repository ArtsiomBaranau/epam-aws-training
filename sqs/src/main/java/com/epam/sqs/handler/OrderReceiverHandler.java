package com.epam.sqs.handler;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.epam.sqs.dto.Log;
import com.epam.sqs.dto.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderReceiverHandler implements RequestHandler<SQSEvent, Void> {

    private static final Long TOTAL_THRESHOLD = 99L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Void handleRequest(SQSEvent event, Context context) {
        LambdaLogger log = context.getLogger();
        log.log("Received an event: " + event);

        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        String orderQueueURL = sqs.getQueueUrl("orders").getQueueUrl();
        String logQueueURL = sqs.getQueueUrl("logs").getQueueUrl();

        event.getRecords().forEach(msg -> {
            Order order = null;
            try {
                order = objectMapper.readValue(msg.getBody(), Order.class);
            } catch (JsonProcessingException e) {
                log.log(e.getMessage());
                return;
            }
            Log logObject = order.getTotal() < TOTAL_THRESHOLD
                    ?
                    Log.builder().order(order).status(Log.Status.ACCEPTED).build()
                    :
                    Log.builder().order(order).status(Log.Status.REJECTED).build();

            String messageBody = null;
            try {
                messageBody = objectMapper.writeValueAsString(logObject);
            } catch (JsonProcessingException e) {
                log.log(e.getMessage());

                return;
            }

            SendMessageRequest logMessage = new SendMessageRequest()
                    .withQueueUrl(logQueueURL)
                    .withMessageBody(messageBody)
                    .withDelaySeconds(5);

            sqs.sendMessage(logMessage);

            sqs.deleteMessage(orderQueueURL, msg.getReceiptHandle());

            log.log("Message was processed! Congrats!");
        });

        return null;
    }
}
