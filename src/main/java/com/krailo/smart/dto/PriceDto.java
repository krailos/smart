package com.krailo.smart.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Builder
@Value
public class PriceDto {
    
    private Integer id;
    private String name;
    private int value;
    private LocalDate date;
    private Integer subjectId;


}
