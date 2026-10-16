package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.TransactionStudentDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.mapper.TransactionStudentMapper;
import com.krailo.smart.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class TransactionStudentService {

    private SubjectService subjectService;
    private LessonService lessonService;
    private TransactionStudentRepository transactionStudentRepository;
    private StudentRepository studentRepository;
    private LessonsStudentsRepository lessonsStudentsRepository;
    private TransactionStudentMapper transactionStudentMapper;

    public List<TransactionStudentDto> findAll() {
        return transactionStudentRepository.findAll().stream().map(transactionStudentMapper::mapEntityToDto).toList();
    }

    public void create(LessonDto lessonDto) {
        System.out.println("----TEST LessonDTO----");
        System.out.println(lessonDto);
        // тільки lesson без колекції (чому її нема хз, щось з кешем хібернейт)
        Lesson lesson = lessonService.findByIdEntity(lessonDto.getId());
        System.out.println("----TEST Lesson----");
        System.out.println(lesson);
        System.out.println("----TEST select on lessons student----");
        // колекція lessonsStudent з бази через select
        List<LessonsStudents> lessonsStudentsSelect = lessonsStudentsRepository.findAllByLesson(lesson);
        lessonsStudentsSelect.forEach(System.out::println);
        lessonsStudentsSelect.stream().map(ls -> ls.getStudent()).forEach(System.out::println);
        lessonsStudentsSelect.stream().map(ls -> ls.getLesson()).forEach(System.out::println);
        System.out.println("----TEST subject----");
        Price subjectPrice = findSubjectPrice(lessonDto);
        System.out.println(subjectPrice);
        System.out.println("----TEST  transaction fill----");
        List<TransactionStudent> transactionStudentFill = fillTransactionStudent(lesson, lessonsStudentsSelect);
        transactionStudentFill.stream().forEach(System.out::println);
        System.out.println("----TEST  transaction calculate and create----");
        calculateSaveTransactionStudent(subjectPrice, transactionStudentFill);
        System.out.println("----END TEST----");
    }

    private Price findSubjectPrice(LessonDto lessonDto) {
        Subject lessonSubject = subjectService.findByIdEntity(lessonDto.getSubjectId());
        return lessonSubject.getPrices().stream()
                .filter(p -> !p.getDate().isAfter(lessonDto.getDate()))
                .max(Comparator.comparing(Price::getDate))
                .orElseGet(null);
    }

    private List<TransactionStudent> fillTransactionStudent( Lesson lesson, List<LessonsStudents> lessonsStudents) {

        LocalDate lessonDate = lesson.getDate();
        Subject lessonSubject = lesson.getSubject();
        List<TransactionStudent> transactionStudentsList = new ArrayList<>();
        for (LessonsStudents ls : lessonsStudents) {
            Student student = ls.getStudent();
            List<StudentsDiscounts> studentsDiscountsList = student.getStudentsDiscounts();
            Optional<StudentsDiscounts> studentDiscountFiltered = studentsDiscountsList.stream()
                    .filter(sd -> sd.getSubject().equals(lessonSubject))
                    .filter(sd -> !sd.getDate().isAfter(lessonDate))
                    .max(Comparator.comparing(StudentsDiscounts::getDate));
            int discountValue = studentDiscountFiltered.isEmpty() ? 0 : studentDiscountFiltered.get().getDiscount().getValue();
            TransactionStudent transactionStudent = TransactionStudent.builder()
                    .date(lessonDate)
                    .lesson(lesson)
                    .student(student)
                    .studentPresent(ls.isPresent())
                    .lessonPayed(ls.isPayed())
                    .discountValue(discountValue)
                    .build();
            transactionStudentsList.add(transactionStudent);
        }
        return transactionStudentsList;
    }

    private void calculateSaveTransactionStudent(Price subjectPrice, List<TransactionStudent> transactionStudentList) {
        for (TransactionStudent ts : transactionStudentList) {
            int discount = ts.getDiscountValue();
            int priceValue = subjectPrice.getValue();
            double credit = priceValue - priceValue * discount / 100.0;
            if (!ts.isStudentPresent() || !ts.isLessonPayed()) {
                transactionStudentRepository.saveAndFlush(ts);
            } else {
                ts.setSubjectFulPrice(priceValue);
                ts.setDiscountValue(discount);
                ts.setCredit(credit);
                transactionStudentRepository.saveAndFlush(ts);
                // зберігаю студента в бд з новим балансом
                Student student = ts.getStudent();
                double balanceUpdate = student.getBalance();
                balanceUpdate = balanceUpdate - credit;
                student.setBalance(balanceUpdate);
                studentRepository.saveAndFlush(student);
            }
        }
    }

}
