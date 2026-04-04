package com.krailo.smart.mapper;

import org.springframework.stereotype.Component;

@Component
public interface Mapper<E, D> {

    E mapDtoToEntityForCreate(D d);

    D mapEntityToDto(E e);

    E mapDtoToEntityForUpdate (D d, E e);

}