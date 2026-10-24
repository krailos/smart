package com.krailo.smart.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Value
public class PriceDto {
    
    private Integer id;
    private String name;
    private BigDecimal value;
    private LocalDate date;
    private Integer subjectId;
    private String subjectName;


}
