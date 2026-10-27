package com.krailo.smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GangDto {

    private Integer id;
    // межі — як у базі: name varchar(50) NOT NULL, description varchar(150);
    // курс і вчитель обов'язкові: GangMapper і сторінки групи розраховують, що вони є
    @NotBlank(message = "Вкажіть назву групи")
    @Size(max = 50, message = "Назва — не довше 50 символів")
    private String name;
    @Size(max = 150, message = "Опис — не довше 150 символів")
    private String description;
    @NotNull(message = "Оберіть курс")
    private Integer subjectId;
    private String subjectNme;
    @NotNull(message = "Оберіть вчителя")
    private Integer teacherId;
    private String teacherFirstName;
    private String teacherLastName;
    private List<GangsStudentsDto> gangsStudentsDto;

}
