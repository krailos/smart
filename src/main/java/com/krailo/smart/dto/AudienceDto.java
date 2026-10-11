package com.krailo.smart.dto;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudienceDto {

    private Integer id;
    private String name;
    private String description;

}
