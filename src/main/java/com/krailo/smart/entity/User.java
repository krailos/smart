package com.krailo.smart.entity;

import com.krailo.smart.enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    // тільки хеш ({bcrypt}$2a$10$...), сам пароль ніде не зберігається
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    // архів: користувач не може увійти, але лишається в історії
    @Column(name = "is_deleted")
    private boolean archived;

}
