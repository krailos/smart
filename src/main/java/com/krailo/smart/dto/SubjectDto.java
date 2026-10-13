package com.krailo.smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {

    private Integer id;
    private String name;
    private String description;
    private List<PriceDto> pricesDto;
    private List<GangDto> gangsDto;

}
