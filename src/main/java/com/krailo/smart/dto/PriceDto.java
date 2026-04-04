package com.krailo.smart.dto;

import com.krailo.smart.entity.Subject;
import lombok.Value;

import java.time.LocalDate;

@Value
public class PriceDto {
    
    private Integer id;
    private String name;
    private int value;
    private LocalDate date;
    private Integer subjectId;
    private Subject subject;

}
