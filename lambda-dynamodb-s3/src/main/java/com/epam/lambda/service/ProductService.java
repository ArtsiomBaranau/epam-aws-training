package com.epam.lambda.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.epam.lambda.model.Product;

import java.util.List;

public class ProductService {
    public static final Regions REGION = Regions.EU_CENTRAL_1;

    private final DynamoDBMapper dynamoDBMapper;

    public ProductService() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
        this.dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
    }

    public List<Product> findAll() {
        PaginatedScanList<Product> products = dynamoDBMapper.scan(Product.class, new DynamoDBScanExpression());
        products.loadAllResults();

        return products;
    }
}
