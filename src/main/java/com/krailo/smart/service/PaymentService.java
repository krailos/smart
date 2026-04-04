package com.krailo.smart.service;

import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import com.krailo.smart.mapper.PaymentMapper;
import com.krailo.smart.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class PaymentService {

    private PaymentRepository paymentRepository;
    private PaymentMapper paymentMapper;

    public List<PaymentDto> findAll() {
        return paymentRepository.findAll().stream().map(paymentMapper::mapEntityToDto).toList();
    }

    public PaymentDto findById(Integer id) {
        return paymentRepository.findById(id).map(paymentMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Audience whith id= %d not exist", id)));
    }
    
    public List<Payment> findAllByStudentEntity(Student student) {
        return paymentRepository.findAllByStudent(student);
    }


    public PaymentDto create(PaymentDto paymentDto) {
        return Optional.of(paymentDto).map(paymentMapper::mapDtoToEntityForCreate).map(paymentRepository::save)
                .map(paymentMapper::mapEntityToDto).orElseThrow();
    }

    public PaymentDto update(Integer id, PaymentDto paymentDto) {
        return paymentRepository.findById(id)
                .map(entity -> paymentMapper.mapDtoToEntityForUpdate(paymentDto, entity))
                .map(paymentRepository::saveAndFlush).map(paymentMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return paymentRepository.findById(id).map(entity -> {
            paymentRepository.delete(entity);
            return true;
        }).orElse(false);
    }

}
