package com.krailo.smart.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class GangDto {

    private Integer id;
    private String name;
    private String description;
    private Integer subjectId;
    private Integer teacherId;
    private List<GangsStudentsDto> gangsStudentsDto;

}
