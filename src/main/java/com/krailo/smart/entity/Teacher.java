package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Data
@EqualsAndHashCode(exclude = "gangs")
@ToString(exclude = "gangs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "second_name")
    private String secondName;
    @Column(name = "last_name")
    private String lastName;
    private String phone;
    private String email;
    private String address;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private String description;
    @OneToMany (mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Gang> gangs;

  

}
