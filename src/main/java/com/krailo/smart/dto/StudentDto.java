package com.krailo.smart.dto;

import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.StudentsDiscounts;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class StudentDto {
 
    private Integer id;
    private List<GangsStudents> gangsStudents;
    private String firstName;
    private String secondName;
    private String lastName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private Gender gender;
    private StudentStatus studentStatus;
    private LocalDate birthDate;
    private String description;
    private List<StudentsDiscounts> studentsDiscounts;
    private List<Payment> payments;
    private List<LessonsStudents> lessonsStudents;

}
