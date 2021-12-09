package com.epam.sqs.handler;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.epam.sqs.dto.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LogReceiverHandler implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public Void handleRequest(SQSEvent event, Context context) {
        LambdaLogger log = context.getLogger();
        log.log("Received a log message: " + event);

        AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        String logQueueURL = sqs.getQueueUrl("logs").getQueueUrl();


        List<InputStream> inputStreams = new LinkedList<>();

        GetObjectRequest getObjectRequest = new GetObjectRequest("sqs-aws-training-logs", "logs.log");
        ByteArrayInputStream inputStreamOldFile = new ByteArrayInputStream(s3.getObject(getObjectRequest).getObjectContent().readAllBytes());
        inputStreams.add(inputStreamOldFile);

        event.getRecords().forEach(msg -> {
            Log logObject = null;
            try {
                logObject = objectMapper.readValue(msg.getBody(), Log.class);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(logObject.toString().getBytes(StandardCharsets.UTF_8));
                inputStreams.add(inputStream);

                sqs.deleteMessage(logQueueURL, msg.getReceiptHandle());
            } catch (JsonProcessingException e) {
                log.log(e.getMessage());

                return;
            }
            InputStream sequenceInputStream = new SequenceInputStream(Collections.enumeration(inputStreams));

            PutObjectRequest logFile = new PutObjectRequest("sqs-aws-training-logs", "logs.log", sequenceInputStream, new ObjectMetadata());
            s3.putObject(logFile);

            log.log("Logs were processed! Congrats!");
        });

        return null;
    }
}
