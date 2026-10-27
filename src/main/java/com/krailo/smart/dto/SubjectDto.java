package com.krailo.smart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {

    private Integer id;
    // межі — як у базі: name varchar(50) NOT NULL, description varchar(150)
    @NotBlank(message = "Вкажіть назву курсу")
    @Size(max = 50, message = "Назва — не довше 50 символів")
    private String name;
    @Size(max = 150, message = "Опис — не довше 150 символів")
    private String description;
    private List<PriceDto> pricesDto;

}
