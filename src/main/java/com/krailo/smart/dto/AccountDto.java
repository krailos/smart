package com.krailo.smart.dto;

import com.krailo.smart.enumeration.AccountType;
import lombok.Value;

@Value
public class AccountDto {

    private Integer id;
    private String code;
    private String name;
    private AccountType type;

}
