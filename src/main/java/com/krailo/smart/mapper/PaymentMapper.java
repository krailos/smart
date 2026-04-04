package com.krailo.smart.mapper;

import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.repository.PaymentRepository;
import com.krailo.smart.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentMapper implements Mapper<Payment, PaymentDto> {
    
    private PaymentRepository paymentRepository;
    private StudentRepository studentRepository;


    @Override
    public Payment mapDtoToEntityForCreate(PaymentDto d) {
        Payment e = new Payment();      
        e.setStudent(studentRepository.findById(d.getStudentId()).get());
        e.setValue(d.getValue());
        e.setDate(d.getDate());
        e.setDescription(d.getDescription());
        return e;
    }

    @Override
    public PaymentDto mapEntityToDto(Payment e) {
        return new PaymentDto(
                e.getId(),                            
                e.getStudent(), 
                e.getStudent().getId(), 
                e.getValue(),
                e.getDate(),  
                e.getDescription());
    }

    @Override
    public Payment mapDtoToEntityForUpdate(PaymentDto d, Payment e) {
        e.setStudent(studentRepository.findById(d.getStudentId()).get());
        e.setValue(d.getValue());
        e.setDate(d.getDate());
        e.setDescription(d.getDescription());
        return e;
    }

}
