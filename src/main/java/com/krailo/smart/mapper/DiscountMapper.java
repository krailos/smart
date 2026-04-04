package com.krailo.smart.mapper;

import com.krailo.smart.dto.DiscountDto;
import com.krailo.smart.entity.Discount;
import com.krailo.smart.repository.StudentRepository;
import com.krailo.smart.repository.SubjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DiscountMapper implements Mapper<Discount, DiscountDto> {
    
    private StudentRepository studentRepository;
    private SubjectRepository subjectRepository;


    @Override
    public Discount mapDtoToEntityForCreate(DiscountDto d) {
       Discount e = new Discount();
        e.setName(d.getName());
        e.setValue(d.getValue());
        e.setDate(d.getDate());
        e.setStudentsDiscounts(d.getStudentsDiscounts());
        return e;
    }

    @Override
    public DiscountDto mapEntityToDto(Discount e) {
        return new DiscountDto(
                e.getId(), 
                e.getName(),
                e.getValue(),
                e.getDate(),
                e.getStudentsDiscounts()
                );
    }

    @Override
    public Discount mapDtoToEntityForUpdate(DiscountDto d, Discount e) {
        e.setName(d.getName());
        e.setValue(d.getValue());
        e.setDate(d.getDate());
        e.setStudentsDiscounts(d.getStudentsDiscounts());
        return e;
    }

}
