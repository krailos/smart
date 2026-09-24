package com.krailo.smart.service;

import com.krailo.smart.dto.BalanceStudentDto;
import com.krailo.smart.entity.BalanceStudent;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.mapper.BalanceStudentMapper;
import com.krailo.smart.repository.BalanceStudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class BalanceStudentService {

    BalanceStudentRepository balanceStudentRepository;
    BalanceStudentMapper balanceStudentMapper;

    public List<BalanceStudentDto>  findAll (){
        return  balanceStudentRepository.findAll().stream().map(balanceStudentMapper::mapEntityToDto).toList();
    }

    public BalanceStudent create (LessonsStudents ls){

        // логіка розрахукну балансу всі необхідні дані беремо з ls та цін та знижок
        // створення balanceStudent та його збереження

        return null;
    }

}
