package com.example.reactivekafkaconsumerandproducer.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("FakeConsumer")
@Data
public class FakeConsumerDTO {
    @JsonProperty("id")
    private String id;
}