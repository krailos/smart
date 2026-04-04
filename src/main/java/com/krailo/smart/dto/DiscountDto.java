package com.krailo.smart.dto;

import com.krailo.smart.entity.StudentsDiscounts;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class DiscountDto {
    

    private Integer id;
    private String name;
    private int value;
    private LocalDate date;
    private List<StudentsDiscounts> studentsDiscounts;

}
