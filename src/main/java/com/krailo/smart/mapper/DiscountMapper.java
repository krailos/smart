package com.krailo.smart.mapper;

import com.krailo.smart.dto.DiscountDto;
import com.krailo.smart.entity.Discount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DiscountMapper implements Mapper<Discount, DiscountDto> {

    @Override
    public Discount mapDtoToEntityForCreate(DiscountDto d) {
       Discount e = new Discount();
        e.setName(d.getName());
        e.setValue(d.getValue());
        e.setDate(d.getDate());
        return e;
    }

    @Override
    public DiscountDto mapEntityToDto(Discount e) {
        return new DiscountDto(
                e.getId(), 
                e.getName(),
                e.getValue(),
                e.getDate()
                );
    }

    @Override
    public Discount mapDtoToEntityForUpdate(DiscountDto d, Discount e) {
        e.setName(d.getName());
        e.setValue(d.getValue());
        e.setDate(d.getDate());
        return e;
    }

}
