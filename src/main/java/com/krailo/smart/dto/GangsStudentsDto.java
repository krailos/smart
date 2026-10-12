package com.krailo.smart.dto;


import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GangsStudentsDto  {

    private Integer id;
    private Integer gangId;
    private StudentDto studentDto;

}
