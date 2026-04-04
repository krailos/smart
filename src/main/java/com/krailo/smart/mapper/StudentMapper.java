package com.krailo.smart.mapper;

import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.Student;
import org.springframework.stereotype.Component;


@Component
public class StudentMapper implements Mapper<Student, StudentDto> {

    @Override
    public Student mapDtoToEntityForCreate(StudentDto d) {
        Student e = new Student();
        e.setGangsStudents(d.getGangsStudents());
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
        e.setStudentsDiscounts(d.getStudentsDiscounts());       
        e.setPayments(d.getPayments());      
        e.setLessonsStudents(d.getLessonsStudents());       
        return e;
    }

    @Override
    public StudentDto mapEntityToDto(Student e) {
        return new StudentDto(
                e.getId(), 
                e.getGangsStudents(), 
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
                e.getStudentsDiscounts(),
                e.getPayments(),
                e.getLessonsStudents()           
                );
        
    }

    @Override
    public Student mapDtoToEntityForUpdate(StudentDto d, Student e) {
        e.setGangsStudents(d.getGangsStudents());
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
        e.setStudentsDiscounts(d.getStudentsDiscounts());       
        e.setPayments(d.getPayments());      
        e.setLessonsStudents(d.getLessonsStudents());      
        return e;
    }

}
