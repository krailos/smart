package com.krailo.smart.dto;

import com.krailo.smart.entity.Student;
import lombok.Value;

import java.time.LocalDate;

@Value
public class PaymentDto {


    private Integer id;  
    private Student student;
    private Integer studentId;   
    private int value;
    private LocalDate date;
    private String description;
    
}
