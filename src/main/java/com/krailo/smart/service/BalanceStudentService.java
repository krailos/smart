package com.krailo.smart.service;

import com.krailo.smart.dto.BalanceStudentDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.mapper.BalanceStudentMapper;
import com.krailo.smart.repository.BalanceStudentRepository;
import com.krailo.smart.repository.LessonRepository;
import com.krailo.smart.repository.StudentRepository;
import com.krailo.smart.repository.StudentsDiscountsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class BalanceStudentService {

    BalanceStudentRepository balanceStudentRepository;
    LessonRepository lessonRepository;
    BalanceStudentMapper balanceStudentMapper;
    StudentsDiscountsRepository studentsDiscountsRepository;
    StudentRepository studentRepository;

    public List<BalanceStudentDto> findAll() {
        return balanceStudentRepository.findAll().stream().map(balanceStudentMapper::mapEntityToDto).toList();
    }

    public void create(Lesson lesson) {
        // беру список студентів на уроці, дату та предмет
        List<LessonsStudents> lessonsStudents = lesson.getLessonsStudents();
        LocalDate lessonDate = lesson.getDate();
        Subject lessonSubject = lesson.getSubject();
        // шукаю вартість уроку
        Price price = lessonSubject.getPrices().stream()
                .filter(p -> !p.getDate().isAfter(lesson.getDate()))
                .sorted(Comparator.comparing(Price::getDate).reversed())
                .findFirst()
                .orElse(null);
        // для кожного студента на уроці розраховую дані для транзакції та записую в бд
        for (LessonsStudents ls : lessonsStudents) {
            // беру з бд студента, щоб все було в одній транзакції та підтягувало потрібні поля з бд
            Student student = studentRepository.findById(ls.getStudent().getId()).orElse(null);
            // шукаю наявність знижок у студента
            List<StudentsDiscounts> studentsDiscountsList = student.getStudentsDiscounts();
            StudentsDiscounts studentDiscount = studentsDiscountsList.stream()
                    .filter(sd -> sd.getSubject().equals(lessonSubject))
                    .filter(sd -> !sd.getDate().isAfter(lessonDate))
                    .max(Comparator.comparing(StudentsDiscounts::getDate)).orElse(null);
            // розраховую вартість уроку з врахуванням знижки
            int discount = studentDiscount == null ? 0 : studentDiscount.getDiscount().getValue();
            int priceSubject = price.getValue();
            double credit = priceSubject - priceSubject * discount / 100.0;
            // розраховую новий баланс
            List <BalanceStudent> bsByDate = balanceStudentRepository.findByDate(student, lessonDate);
            BalanceStudent bsByDateWhitBalance = bsByDate.isEmpty() ? null : bsByDate.get(0);
            double balancePrev = bsByDateWhitBalance == null ? 0 : bsByDateWhitBalance.getBalance();
            double balance = balancePrev - credit;
            // зберігаю в бд з новими даними дані по транзакції
            BalanceStudent bs = new BalanceStudent();
            bs.setStudent(student);
            bs.setDate(lessonDate);
            bs.setLesson(lesson);
            bs.setCredit(credit);
            bs.setBalance(balance);
            balanceStudentRepository.save(bs);
            // зберігаю в бд з новим балансом студента
            student.setBalance(balance);
            studentRepository.save(student);
        }
    }


}
