package com.epam.lambda.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Products")
public class Product {
    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private String productName;
    @DynamoDBAttribute
    private String productUrl;
    @DynamoDBAttribute
    private Double productPrice;
}
