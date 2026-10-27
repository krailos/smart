package com.krailo.smart.mapper;

import com.krailo.smart.dto.StudentsDiscountsDto;
import com.krailo.smart.entity.StudentsDiscounts;
import com.krailo.smart.exception.NotFoundException;
import com.krailo.smart.repository.DiscountRepository;
import com.krailo.smart.repository.StudentRepository;
import com.krailo.smart.repository.SubjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StudentsDiscountsMapper implements Mapper<StudentsDiscounts, StudentsDiscountsDto> {
    
    DiscountRepository discountRepository;
    StudentRepository studentRepository;
    SubjectRepository subjectRepository;

    @Override
    public StudentsDiscounts mapDtoToEntityForCreate(StudentsDiscountsDto d) {
        StudentsDiscounts e = new StudentsDiscounts();
        e.setId(d.getId());
        e.setDiscount(discountRepository.findById(d.getDiscountId())
                .orElseThrow(() -> new NotFoundException("Знижка", d.getDiscountId())));
        e.setStudent(studentRepository.findById(d.getStudentId())
                .orElseThrow(() -> new NotFoundException("Учень", d.getStudentId())));
        e.setSubject(subjectRepository.findById(d.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Курс", d.getSubjectId())));
        e.setDate(d.getDate());
        return e;
    }

    @Override
    public StudentsDiscountsDto mapEntityToDto(StudentsDiscounts e) {
        return new StudentsDiscountsDto(
                e.getId(),
                e.getDiscount().getId(),
                e.getDiscount().getName(),
                e.getDiscount().getValue(),
                e.getStudent().getId(),
                e.getStudent().getLastName() + " " + e.getStudent().getFirstName(),
                e.getSubject().getId(),
                e.getSubject().getName(),
                e.getDate());
    }

    @Override
    public StudentsDiscounts mapDtoToEntityForUpdate(StudentsDiscountsDto d, StudentsDiscounts e) {
        e.setId(d.getId());
        e.setDiscount(discountRepository.findById(d.getDiscountId())
                .orElseThrow(() -> new NotFoundException("Знижка", d.getDiscountId())));
        e.setStudent(studentRepository.findById(d.getStudentId())
                .orElseThrow(() -> new NotFoundException("Учень", d.getStudentId())));
        e.setSubject(subjectRepository.findById(d.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Курс", d.getSubjectId())));
        e.setDate(d.getDate());
        return e;
    }

}
