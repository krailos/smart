package com.krailo.smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudienceDto {

    private Integer id;
    // межі — як у базі: name varchar(50) NOT NULL, description varchar(150)
    @NotBlank(message = "Вкажіть назву аудиторії")
    @Size(max = 50, message = "Назва — не довше 50 символів")
    private String name;
    @Size(max = 150, message = "Опис — не довше 150 символів")
    private String description;

}
