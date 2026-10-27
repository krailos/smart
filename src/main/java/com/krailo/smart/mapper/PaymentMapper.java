package com.krailo.smart.mapper;

import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.exception.NotFoundException;
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
        e.setStudent(studentRepository.findById(d.getStudentId())
                .orElseThrow(() -> new NotFoundException("Учень", d.getStudentId())));
        e.setValue(d.getValue());
        e.setDate(d.getDate());
        e.setDescription(d.getDescription());
        return e;
    }

    @Override
    public PaymentDto mapEntityToDto(Payment e) {
        return mapEntityToDto(e, paymentRepository.existsByStornoOfId(e.getId()));
    }

    // для списку: чи сторновано — уже відомо (PaymentRepository.findStornedIds), без запиту на кожну оплату
    public PaymentDto mapEntityToDto(Payment e, boolean storned) {
        return new PaymentDto(
                e.getId(),                            
                e.getStudent().getLastName() + " " + e.getStudent().getFirstName(),
                e.getStudent().getId(), 
                e.getValue(),
                e.getDate(),
                e.getDescription(),
                e.getStornoOf() == null ? null : e.getStornoOf().getId(),
                storned);
    }

    // оплати не редагуються — помилку виправляють сторнуванням (PaymentReceivingService.stornoPayment)
    @Override
    public Payment mapDtoToEntityForUpdate(PaymentDto d, Payment e) {
        throw new UnsupportedOperationException("Оплати не редагуються — використовуйте сторнування");
    }

}
