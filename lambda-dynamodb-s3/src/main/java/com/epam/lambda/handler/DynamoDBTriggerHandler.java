package com.epam.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.epam.lambda.model.Product;
import com.epam.lambda.service.ProductService;

import java.io.ByteArrayInputStream;
import java.util.List;

public class DynamoDBTriggerHandler implements RequestHandler<DynamodbEvent, Void> {

    private final ProductService productService = new ProductService();
    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

    @Override
    public Void handleRequest(DynamodbEvent input, Context context) {

        String html = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Products!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <table>\n" +
                "\t  <tr>\n" +
                "\t    <th>ID</th>\n" +
                "\t    <th>Name</th>\n" +
                "\t    <th>Url</th>\n" +
                "\t    <th>Price</th>\n" +
                "\t  </tr>\n" +
                "\t  $PRODUCT_DATA_ROWS\n" +
                "\t</table>\n" +
                "  </body>\n" +
                "</html>";

        html = html.replace("$PRODUCT_DATA_ROWS", this.buildTableRowsFromProducts(productService.findAll()));

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/html");

        PutObjectRequest htmlFile = new PutObjectRequest("lambda-dynamodb-s3-website-hosting", "index.html", new ByteArrayInputStream(html.getBytes()), objectMetadata);

        s3.putObject(htmlFile);

        return null;
    }

    public String buildTableRowsFromProducts(List<Product> products) {
        String pattern = "<tr>\n" +
                "  <td>$ID</td>\n" +
                "  <td>$NAME</td>\n" +
                "  <td>$URL</td>\n" +
                "  <td>$PRICE</td>\n" +
                "</tr>";

        StringBuilder stringBuilder = new StringBuilder();

        products.forEach(product -> {
            String filledPattern = pattern.replace("$ID", product.getId());
            filledPattern = filledPattern.replace("$NAME", product.getProductName());
            filledPattern = filledPattern.replace("$URL", product.getProductUrl());
            filledPattern = filledPattern.replace("$PRICE", product.getProductPrice().toString());
            stringBuilder.append(filledPattern);
        });

        return stringBuilder.toString();
    }
}
