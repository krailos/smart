package com.krailo.smart.dto;

import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.entity.Teacher;
import lombok.Value;

import java.util.List;

@Value
public class GangDto {
    
    private Integer id;
    private String name;
    private String description;
    private Subject subject;
    private Integer subjectId;
    private Teacher teacher;
    private Integer teacherId;
    private List<GangsStudents> gangStudents;

}
