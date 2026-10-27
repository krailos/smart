package com.krailo.smart.dto;

import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class StudentDto {
 
    private Integer id;
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
    private BigDecimal balance; // може бути null: у формах учня цього поля немає
    // кількості — лише для списку учнів (StudentService.findAllByPage); Integer, бо з форм учня приходить null
    private Integer gangsCount;
    private Integer discountsCount;

}
