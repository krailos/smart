package com.krailo.smart.service;


import com.krailo.smart.dto.AccountDto;
import com.krailo.smart.mapper.AccountMapper;
import com.krailo.smart.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class AccountService {

    private AccountRepository accountRepository;
    private AccountMapper accountMapper;

    public List<AccountDto> findAll() {
        return accountRepository.findAll().stream().map(accountMapper::mapEntityToDto).toList();
    }

    public AccountDto findById(Integer id) {
        return accountRepository.findById(id).map(accountMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Audience whith id= %d not exist", id)));
    }

    public AccountDto create(AccountDto accountDto) {
        return Optional.of(accountDto).map(accountMapper::mapDtoToEntityForCreate).map(accountRepository::save)
                .map(accountMapper::mapEntityToDto).orElseThrow();
    }

    public AccountDto update(Integer id, AccountDto accountDto) {
        return accountRepository.findById(id)
                .map(entity -> accountMapper.mapDtoToEntityForUpdate(accountDto, entity))
                .map(accountRepository::saveAndFlush).map(accountMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return accountRepository.findById(id).map(entity -> {
            accountRepository.delete(entity);
            return true;
        }).orElse(false);
    }

}