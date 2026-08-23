package com.krailo.smart.mapper;

import com.krailo.smart.dto.BalanceStudentDto;
import com.krailo.smart.entity.BalanceStudent;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import com.krailo.smart.repository.LessonRepository;
import com.krailo.smart.repository.PaymentRepository;
import com.krailo.smart.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class BalanceStudentMapper implements Mapper<BalanceStudent, BalanceStudentDto> {
    LessonRepository lessonRepository;
    PaymentRepository paymentRepository;
    StudentRepository studentRepository;

    @Override
    public BalanceStudent mapDtoToEntityForCreate(BalanceStudentDto dto) {
        BalanceStudent entity = new BalanceStudent();
        entity.setDate(dto.getDate());
        entity.setLesson(lessonRepository.findById(dto.getLessonId()).orElse(null));
        entity.setPayment(paymentRepository.findById(dto.getPaymentId()).orElse(null));
        entity.setStudent(studentRepository.findById(dto.getStudentId()).orElse(null));
        entity.setDebit(dto.getDebit());
        entity.setCredit(dto.getCredit());
        entity.setBalance(dto.getBalance());
        return entity;
    }

    @Override
    public BalanceStudentDto mapEntityToDto(BalanceStudent entity) {
        return new BalanceStudentDto(
                entity.getId(),
                entity.getDate(),
                Optional.ofNullable(entity.getLesson()).orElse(null),
                Optional.ofNullable(entity).map(BalanceStudent::getLesson).map(Lesson::getId).orElse(null),
                Optional.ofNullable(entity.getPayment()).orElse(null),
                Optional.ofNullable(entity).map(BalanceStudent::getPayment).map(Payment::getId).orElse(null),
                Optional.ofNullable(entity.getStudent()).orElse(null),
                Optional.ofNullable(entity).map(BalanceStudent::getStudent).map(Student::getId).orElse(null),
                entity.getDebit(),
                entity.getCredit(),
                entity.getBalance()
        );
    }

    @Override
    public BalanceStudent mapDtoToEntityForUpdate(BalanceStudentDto dto, BalanceStudent entity) {
        entity.setId(dto.getId());
        entity.setDate(dto.getDate());
        entity.setLesson(lessonRepository.findById(dto.getLessonId()).orElse(null));
        entity.setPayment(paymentRepository.findById(dto.getPaymentId()).orElse(null));
        entity.setStudent(studentRepository.findById(dto.getStudentId()).orElse(null));
        entity.setDebit(dto.getDebit());
        entity.setCredit(dto.getCredit());
        entity.setBalance(dto.getBalance());
        return entity;
    }
}
