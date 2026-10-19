package com.krailo.smart.mapper;

import com.krailo.smart.dto.TransactionStudentDto;
import com.krailo.smart.entity.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class TransactionStudentMapper implements Mapper<TransactionStudent, TransactionStudentDto> {

    @Override
    public TransactionStudent mapDtoToEntityForCreate(TransactionStudentDto dto) {
        return TransactionStudent.builder()
                .lesson(Lesson.builder().id(dto.getLessonId()).build())
                .payment(Payment.builder().id(dto.getPaymentId()).build())
                .student(Student.builder().id(dto.getStudentId()).build())
                .studentPresent(dto.isStudentPresent())
                .lessonPayed(dto.isLessonPayed())
                .subjectFulPrice(dto.getSubjectFulPrice())
                .discountPercentage(dto.getDiscountPercentage())
                .discountSum(dto.getDiscountSum())
                .debit(dto.getDebit())
                .credit(dto.getCredit())
                .balance(dto.getBalance())
                .build();
    }

    @Override
    public TransactionStudentDto mapEntityToDto(TransactionStudent entity) {
        return TransactionStudentDto.builder()
                .id(entity.getId())
                .lessonId(Optional.ofNullable(entity.getLesson())
                        .map(Lesson::getId)
                        .orElse(null))
                .lessonDate(Optional.ofNullable(entity.getLesson())
                        .map(Lesson::getDate)
                        .orElse(null))
                .subjectName(Optional.ofNullable(entity.getLesson())
                        .map(Lesson::getSubject)
                        .map(Subject::getName)
                        .orElse(null))
                .paymentId(Optional.ofNullable(entity.getPayment())
                        .map(Payment::getId)
                        .orElse(null))
                .paymentDate(Optional.ofNullable(entity.getPayment())
                        .map(Payment::getDate)
                        .orElse(null))
                .studentId(entity.getStudent().getId())
                .studentLastName(entity.getStudent().getLastName())
                .studentFirstName(entity.getStudent().getFirstName())
                .studentPresent(entity.isStudentPresent())
                .lessonPayed(entity.isLessonPayed())
                .subjectFulPrice(entity.getSubjectFulPrice())
                .discountPercentage(entity.getDiscountPercentage())
                .discountSum(entity.getDiscountSum())
                .debit(entity.getDebit())
                .credit(entity.getCredit())
                .balance(entity.getBalance())
                .build();
    }

    @Override
    public TransactionStudent mapDtoToEntityForUpdate(TransactionStudentDto dto, TransactionStudent entity) {
        entity.setId(dto.getId());
        entity.setLesson(Lesson.builder().id(dto.getLessonId()).build());
        entity.setPayment(Payment.builder().id(dto.getPaymentId()).build());
        entity.setStudent(Student.builder().id(dto.getStudentId()).build());
        entity.setStudentPresent(dto.isStudentPresent());
        entity.setLessonPayed(dto.isLessonPayed());
        entity.setSubjectFulPrice(dto.getSubjectFulPrice());
        entity.setDiscountPercentage(dto.getDiscountPercentage());
        entity.setDiscountSum(dto.getDiscountSum());
        entity.setDebit(dto.getDebit());
        entity.setCredit(dto.getCredit());
        entity.setBalance(dto.getBalance());
        return entity;
    }
}
