package com.epam.lambda.handler;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.epam.lambda.model.Product;
import lombok.SneakyThrows;

import java.util.UUID;

public class UpdateProductRequestHandler implements RequestHandler<Product, Product> {

    private static final String DYNAMODB_TABLE_NAME = "Products";
    private static final Regions REGION = Regions.EU_CENTRAL_1;

    private DynamoDB dynamoDB;

    @Override
    @SneakyThrows
    public Product handleRequest(Product product, Context context) {
        LambdaLogger log = context.getLogger();

        log.log("Received product: " + product.toString());

        this.initDynamoDbClient();

        log.log("DynamoDB client was initialized.");

        UpdateItemResult updateItemResult = persistData(product);

        log.log("Updated product: " + updateItemResult.toString());

        return Product.builder()
                .id(product.getId())
                .productName(updateItemResult.getAttributes().get("productName").getS())
                .productUrl(updateItemResult.getAttributes().get("productUrl").getS())
                .productPrice(Double.valueOf(updateItemResult.getAttributes().get("productPrice").getN()))
                .build();
    }

    private UpdateItemResult persistData(Product product) {
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        if (product.getId() != null) {
            Item item = table.getItem(new GetItemSpec().withPrimaryKey("id", product.getId()));

            if (item == null)
                throw new RuntimeException("Product with ID: " + product.getId() + " doesn't exist");
        }

        return table.updateItem(new UpdateItemSpec().withPrimaryKey("id", product.getId())
                        .withUpdateExpression("set productName=:n, productUrl=:u, productPrice=:p")
                        .withValueMap(new ValueMap()
                                .withString(":n", product.getProductName())
                                .withString(":u", product.getProductUrl())
                                .withNumber(":p", product.getProductPrice()))
                        .withReturnValues(ReturnValue.UPDATED_NEW))
                .getUpdateItemResult();
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(REGION).build();
        this.dynamoDB = new DynamoDB(dynamoDBClient);
    }
}
