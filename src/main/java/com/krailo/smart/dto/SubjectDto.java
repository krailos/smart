package com.krailo.smart.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class SubjectDto {
    
    private Integer id;
    private String name;
    private String description;
    private List<PriceDto> pricesDto;
    private List<GangDto> gangsDto;

}
