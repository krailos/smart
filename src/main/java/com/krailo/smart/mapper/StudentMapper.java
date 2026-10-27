package com.krailo.smart.mapper;

import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.Student;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class StudentMapper implements Mapper<Student, StudentDto> {

    @Override
    public Student mapDtoToEntityForCreate(StudentDto d) {
        Student e = new Student();
        e.setFirstName(d.getFirstName());
        e.setSecondName(d.getSecondName());
        e.setLastName(d.getLastName());
        e.setContactName(d.getContactName());
        e.setPhone(d.getPhone());
        e.setEmail(d.getEmail());
        e.setAddress(d.getAddress());
        e.setGender(d.getGender());
        e.setStudentStatus(d.getStudentStatus());
        e.setBirthDate(d.getBirthDate()); 
        e.setDescription(d.getDescription());
        e.setBalance(BigDecimal.ZERO); // новий учень завжди починає з нульовим балансом
        return e;
    }

    // без кількостей — для картки, форм і випадаючих меню: групи й знижки з бази не вантажаться
    @Override
    public StudentDto mapEntityToDto(Student e) {
        return mapEntityToDto(e, null, null);
    }

    // для списку учнів (StudentService.findAllByPage), де показуємо кількість груп і знижок
    public StudentDto mapEntityToDto(Student e, Integer gangsCount, Integer discountsCount) {
        return new StudentDto(
                e.getId(), 
                e.getFirstName(), 
                e.getSecondName(), 
                e.getLastName(),
                e.getContactName(),
                e.getPhone(), 
                e.getEmail(), 
                e.getAddress(), 
                e.getGender(), 
                e.getStudentStatus(),
                e.getBirthDate(), 
                e.getDescription(),
                e.getBalance(),
                gangsCount,
                discountsCount
                );
        
    }

    @Override
    public Student mapDtoToEntityForUpdate(StudentDto d, Student e) {
        e.setFirstName(d.getFirstName());
        e.setSecondName(d.getSecondName());
        e.setLastName(d.getLastName());
        e.setContactName(d.getContactName());
        e.setPhone(d.getPhone());
        e.setEmail(d.getEmail());
        e.setAddress(d.getAddress());
        e.setGender(d.getGender());
        e.setStudentStatus(d.getStudentStatus());
        e.setBirthDate(d.getBirthDate()); 
        e.setDescription(d.getDescription());
        // баланс НЕ беремо з форми: його змінюють тільки оплати та уроки (TransactionStudentService)
        return e;
    }

}
