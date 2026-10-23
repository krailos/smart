package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Раніше тут був @SoftDelete: тоді розклад і уроки з "видаленою" аудиторією отримували audience = null
// і сторінки падали. Тепер аудиторію архівують — як вчителів, учнів, курси, групи, знижки.
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
    // архів: запис зникає зі списків і випадаючих меню форм, але лишається в історії (уроки, розклад)
    @Column(name = "is_deleted")
    private boolean archived;

}
