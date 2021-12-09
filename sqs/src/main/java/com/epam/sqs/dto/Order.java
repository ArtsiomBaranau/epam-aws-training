package com.epam.sqs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Type type;
    private Long total;
    private Integer volume;
    private Integer quantity;

    public enum Type {
        LIQUID, COUNTABLE
    }
}
