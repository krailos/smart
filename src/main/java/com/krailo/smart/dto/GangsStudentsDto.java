package com.krailo.smart.dto;


import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class GangsStudentsDto  {

    private Integer id;
    private Integer gangId;
    private Integer studentId;

}
