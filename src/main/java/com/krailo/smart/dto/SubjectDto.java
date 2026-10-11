package com.krailo.smart.dto;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {

    private Integer id;
    private String name;
    private String description;
    // private List<PriceDto> pricesDto;
    // private List<GangDto> gangsDto;

}
