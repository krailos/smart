package com.krailo.smart.mapper;


import com.krailo.smart.dto.AccountDto;
import com.krailo.smart.entity.Account;
import org.springframework.stereotype.Component;


@Component
public class AccountMapper implements Mapper<Account, AccountDto> {

    @Override
    public AccountDto mapEntityToDto(Account entity) {
        return new AccountDto(entity.getId(), entity.getCode(), entity.getName(),
                entity.getType());
    }

    @Override
    public Account mapDtoToEntityForCreate(AccountDto dto) {
        Account account = new Account();
        account.setCode(dto.getCode());
        account.setName(dto.getName());
        account.setType(dto.getType());
        return account;
    }

    @Override
    public Account mapDtoToEntityForUpdate(AccountDto dto, Account entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        return entity;
    }

}
