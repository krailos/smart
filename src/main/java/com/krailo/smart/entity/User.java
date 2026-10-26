package com.krailo.smart.entity;

import com.krailo.smart.enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


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
    // для ролі TEACHER — хто це з вчителів (його заняття, уроки, учні — "свої"); у адміна null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Teacher teacher;
    // пароль тимчасовий (задав адмін) — після входу користувач спершу має його змінити
    @Column(name = "must_change_password")
    private boolean mustChangePassword;
    // архів: користувач не може увійти, але лишається в історії
    @Column(name = "is_deleted")
    private boolean archived;

}
