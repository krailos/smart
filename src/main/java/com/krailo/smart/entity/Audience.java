package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@SoftDelete(columnName = "is_deleted")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Audience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    @Column(name = "is_deleted", insertable = false, updatable = false)
    private boolean deleted;

}
