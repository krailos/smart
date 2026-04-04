package com.krailo.smart.dto;

import com.krailo.smart.entity.Price;
import lombok.Value;

import java.util.List;

@Value
public class SubjectDto {
    
    private Integer id;
    private String name;
    private String description;
    private List<Price> prices;

}
