package com.krailo.smart.dto;


import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GangsStudentsDto  {

    private Integer id;
    private Integer gangId;
    private String gangName;
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;

}
