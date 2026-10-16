package com.krailo.smart.mapper;

import com.krailo.smart.dto.TransactionStudentDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import com.krailo.smart.entity.TransactionStudent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TransactionStudentMapper implements Mapper<TransactionStudent, TransactionStudentDto> {

    @Override
    public TransactionStudent mapDtoToEntityForCreate(TransactionStudentDto dto) {
        return TransactionStudent.builder()
                .date(dto.getTransactionDate())
                .lesson(Lesson.builder().id(dto.getLessonId()).build())
                .payment(Payment.builder().id(dto.getPaymentId()).build())
                .student(Student.builder().id(dto.getStudentId()).build())
                .studentPresent(dto.isStudentPresent())
                .lessonPayed(dto.isLessonPayed())
                .subjectFulPrice(dto.getSubjectFulPrice())
                .discountValue(dto.getDiscountValue())
                .debit(dto.getDebit())
                .credit(dto.getCredit())
                .balance(dto.getBalance())
                .build();
    }

    @Override
    public TransactionStudentDto mapEntityToDto(TransactionStudent entity) {
        return TransactionStudentDto.builder()
                .id(entity.getId())
                .transactionDate(entity.getDate())
                .lessonId(entity.getLesson().getId())
                .lessonDate(entity.getLesson().getDate())
                .subjectName(entity.getLesson().getSubject().getName())
                // .paymentId(entity.getPayment().getId())
                //.paymentDate(entity.getPayment().getDate())
                .studentId(entity.getStudent().getId())
                .studentLastName(entity.getStudent().getLastName())
                .studentFirstName(entity.getStudent().getFirstName())
                .studentPresent(entity.isStudentPresent())
                .lessonPayed(entity.isLessonPayed())
                .subjectFulPrice(entity.getSubjectFulPrice())
                .discountValue(entity.getDiscountValue())
                .debit(entity.getDebit())
                .credit(entity.getCredit())
                .balance(entity.getBalance())
                .build();
    }

    @Override
    public TransactionStudent mapDtoToEntityForUpdate(TransactionStudentDto dto, TransactionStudent entity) {
        entity.setId(dto.getId());
        entity.setDate(dto.getTransactionDate());
        entity.setLesson(Lesson.builder().id(dto.getLessonId()).build());
        entity.setPayment(Payment.builder().id(dto.getPaymentId()).build());
        entity.setStudent(Student.builder().id(dto.getStudentId()).build());
        entity.setStudentPresent(dto.isStudentPresent());
        entity.setLessonPayed(dto.isLessonPayed());
        entity.setSubjectFulPrice(dto.getSubjectFulPrice());
        entity.setDiscountValue(dto.getDiscountValue());
        entity.setDebit(dto.getDebit());
        entity.setCredit(dto.getCredit());
        entity.setBalance(dto.getBalance());
        return entity;
    }
}
