package com.krailo.smart.service;

import com.krailo.smart.dto.BalanceStudentDto;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.mapper.BalanceStudentMapper;
import com.krailo.smart.repository.BalanceStudentRepository;
import com.krailo.smart.repository.LessonRepository;
import com.krailo.smart.repository.StudentsDiscountsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<BalanceStudentDto> findAll() {
        return balanceStudentRepository.findAll().stream().map(balanceStudentMapper::mapEntityToDto).toList();
    }

    public void create (LessonDto lessonDto) {

        // логіка розрахукну балансу всі необхідні дані беремо з ls та цін та знижок
        // беру варість уроку
        Price price = lessonDto.getSubject().getPrices().stream()
                .filter(p -> !p.getDate().isBefore(lessonDto.getDate()))
                .max(Comparator.comparing(Price::getDate)).orElse(null);

//         Price priceSubject = lessonDto.getSubject().getPrices().stream()
//                 .filter(p -> !p.getDate().isBefore(lessonDto.getDate()))
//                 .sorted(Comparator.comparing(Price::getDate).reversed())
//                 .findFirst()
//                 .orElse(null);

        List<LessonsStudents> lsl = lessonDto.getLessonsStudents();
        List<BalanceStudent> bsl = new ArrayList<BalanceStudent>();
        Lesson lesson = lessonRepository.findById(lessonDto.getId()).orElse(null);
        LocalDate lessonDate = lessonDto.getDate();
        Subject subject = lessonDto.getSubject();
        for (LessonsStudents ls : lsl){
            Student student = ls.getStudent();
            List<StudentsDiscounts> studentsDiscountsList = student.getStudentsDiscounts();
            StudentsDiscounts studentDiscount = studentsDiscountsList.stream()
                    .filter(sd -> sd.getSubject().equals(subject))
                    .filter(sd -> !sd.getDate().isBefore(lessonDate))
                    .max(Comparator.comparing(StudentsDiscounts::getDate)).orElse(null);
            int discountValue =  studentDiscount == null ? 100 : studentDiscount.getDiscount().getValue();
            BalanceStudent bs = new BalanceStudent();
            bs.setStudent(student);
            bs.setDate(lessonDate);
            bs.setLesson(lesson);
            double credit = price.getValue() * (price.getValue() / 100.0);
            bs.setCredit(credit);
            balanceStudentRepository.save(bs);
        }
    }


}
