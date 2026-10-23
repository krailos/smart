package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.dto.TransactionStudentDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.exception.PriceNotFoundException;
import com.krailo.smart.mapper.TransactionStudentMapper;
import com.krailo.smart.repository.LessonsStudentsRepository;
import com.krailo.smart.repository.StudentRepository;
import com.krailo.smart.repository.TransactionStudentRepository;
import jakarta.persistence.EntityManager;
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
    private PaymentService paymentService;
    private StudentService studentService;
    private EntityManager entityManager;

    public List<TransactionStudentDto> findAll() {
        return transactionStudentRepository.findAll().stream().map(transactionStudentMapper::mapEntityToDto).toList();
    }

    public void createByPayment (PaymentDto paymentDto){
        entityManager.clear();
        Payment payment = paymentService.findByIdEntity(paymentDto.getId());
        Student student = studentService.findByIdEntity(paymentDto.getStudentId());
        double credit = paymentDto.getValue();
        double balancePrev = student.getBalance();
        double balance = balancePrev + credit;
        TransactionStudent transactionStudent = TransactionStudent.builder()
                .payment(payment)
                .student(student)
                .credit(credit)
                .balance(balance)
                .build();
        transactionStudentRepository.saveAndFlush(transactionStudent);
        student.setBalance(balance);
        studentRepository.saveAndFlush(student);
    }

    public void createByLesson(LessonDto lessonDto) {
        entityManager.clear();
        System.out.println("----TEST LessonDTO----");
        System.out.println(lessonDto);
        // тільки lesson без колекції (чому її нема хз, щось з кешем хібернейт)
        Lesson lesson = lessonService.findByIdEntity(lessonDto.getId());
        System.out.println("----TEST Lesson----");
        System.out.println(lesson);
        System.out.println("----TEST lessonsStudents on lessons----");
        List<LessonsStudents> lessonsStudents = lessonsStudentsRepository.findAllByLesson(lesson);
        System.out.println(lessonsStudents.stream().toList());
        // System.out.println("----TEST select on lessons student----");
        // колекція lessonsStudent з бази через select
        // List<LessonsStudents> lessonsStudentsSelected = lessonsStudentsRepository.findAllByLesson(lesson);
        lessonsStudents.forEach(System.out::println);
        lessonsStudents.stream().map(ls -> ls.getStudent()).forEach(System.out::println);
        lessonsStudents.stream().map(ls -> ls.getLesson()).forEach(System.out::println);
        System.out.println("----TEST subject----");
        Price subjectPrice = findSubjectPrice(lessonDto);
        System.out.println(subjectPrice);
        System.out.println("----TEST  transaction fill----");
        List<TransactionStudent> transactionStudentFill = fillTransactionByLesson(lesson, lessonsStudents);
        transactionStudentFill.stream().forEach(System.out::println);
        System.out.println("----TEST  transaction calculate and create----");
        calculateSaveTransactionByLesson(subjectPrice, transactionStudentFill);
        System.out.println("----END TEST----");
    }

    private Price findSubjectPrice(LessonDto lessonDto) {
        Subject lessonSubject = subjectService.findByIdEntity(lessonDto.getSubjectId());
        return lessonSubject.getPrices().stream()
                .filter(p -> !p.getDate().isAfter(lessonDto.getDate()))
                .max(Comparator.comparing(Price::getDate))
                // orElseGet(null) кидав NullPointerException — тепер зрозуміла помилка для користувача
                .orElseThrow(() -> new PriceNotFoundException(String.format(
                        "Для курсу «%s» немає ціни на %s. Додайте ціну курсу і проведіть урок ще раз",
                        lessonSubject.getName(), lessonDto.getDate())));
    }

    private List<TransactionStudent> fillTransactionByLesson(Lesson lesson, List<LessonsStudents> lessonsStudents) {

        LocalDate lessonDate = lesson.getDate();
        System.out.println("lessonDate -------" + lessonDate.toString());
        Subject lessonSubject = lesson.getSubject();
        System.out.println("lessonSubject-----" + lessonSubject.toString());
        List<TransactionStudent> transactionStudentsList = new ArrayList<>();
        for (LessonsStudents ls : lessonsStudents) {
            Student student = ls.getStudent();
            System.out.println("-----Test discounts");
            List<StudentsDiscounts> studentsDiscountsList = student.getStudentsDiscounts();
            studentsDiscountsList.forEach(System.out::println);
            Optional<StudentsDiscounts> studentDiscountFiltered = studentsDiscountsList.stream()
                    .filter(sd -> sd.getSubject().equals(lessonSubject))
                    .filter(sd -> !sd.getDate().isAfter(lessonDate))
                    .max(Comparator.comparing(StudentsDiscounts::getDate));
            System.out.println("studentDiscountFiltered" + studentDiscountFiltered.toString() );
            int discountPercentage = studentDiscountFiltered.isEmpty() ? 0 : studentDiscountFiltered.get().getDiscount().getValue();
            TransactionStudent transactionStudent = TransactionStudent.builder()
                    .lesson(lesson)
                    .student(student)
                    .studentPresent(ls.isPresent())
                    .lessonPayed(ls.isPayed())
                    .discountPercentage(discountPercentage)
                    .build();
            transactionStudentsList.add(transactionStudent);
        }
        return transactionStudentsList;
    }

    private void calculateSaveTransactionByLesson(Price subjectPrice, List<TransactionStudent> transactionStudentList) {
        for (TransactionStudent ts : transactionStudentList) {
            int discountPercentage = ts.getDiscountPercentage();
            int priceValue = subjectPrice.getValue();
            double discountSum = priceValue * discountPercentage / 100.0;
            double credit = priceValue - discountSum;
            if (!ts.isStudentPresent() || !ts.isLessonPayed()) {
                transactionStudentRepository.saveAndFlush(ts);
            } else {
                ts.setSubjectFulPrice(priceValue);
                ts.setDiscountPercentage(discountPercentage);
                ts.setDiscountSum(discountSum);
                ts.setCredit(credit);
                Student student = ts.getStudent();
                double balanceUpdate = student.getBalance();
                balanceUpdate = balanceUpdate - credit;
                ts.setBalance(balanceUpdate);
                transactionStudentRepository.saveAndFlush(ts);
                // зберігаю студента в бд з новим балансом
                student.setBalance(balanceUpdate);
                studentRepository.saveAndFlush(student);
            }
        }
    }

    // Сторно уроку: для кожного списання з цього уроку — запис з протилежною сумою, гроші повертаються на баланс.
    // Для уроку credit — це сума, яку СПИСАЛИ (баланс -= credit), тому сторно: credit = -списане, баланс += списане.
    public void createStornoByLesson(Lesson lesson) {
        for (TransactionStudent charge : transactionStudentRepository.findAllByLesson(lesson)) {
            if (charge.getCredit() == 0) {
                continue; // учень був відсутній або урок безоплатний — списання не було
            }
            Student student = charge.getStudent();
            double balanceUpdate = student.getBalance() + charge.getCredit();
            TransactionStudent storno = TransactionStudent.builder()
                    .lesson(lesson)
                    .student(student)
                    .studentPresent(charge.isStudentPresent())
                    .lessonPayed(charge.isLessonPayed())
                    .subjectFulPrice(charge.getSubjectFulPrice())
                    .discountPercentage(charge.getDiscountPercentage())
                    .discountSum(-charge.getDiscountSum())
                    .credit(-charge.getCredit())
                    .balance(balanceUpdate)
                    .build();
            transactionStudentRepository.saveAndFlush(storno);
            student.setBalance(balanceUpdate);
            studentRepository.saveAndFlush(student);
        }
    }

}
