package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"prices", "gangs"})
@ToString(exclude = {"prices", "gangs"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<Price> prices;
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<Gang> gangs;
    
}
