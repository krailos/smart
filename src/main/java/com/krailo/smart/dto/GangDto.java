package com.krailo.smart.dto;

import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GangDto {

    private Integer id;
    private String name;
    private String description;
    private Integer subjectId;
    private Integer teacherId;
    private List<GangsStudentsDto> gangsStudentsDto;

}
