package com.epam.sqs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {
    private Order order;
    private Status status;

    public enum Status {
        ACCEPTED, REJECTED
    }
}
