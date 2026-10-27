package com.krailo.smart.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class DiscountDto {
    

    private Integer id;
    private String name;
    private int value;
    private LocalDate date;

}
