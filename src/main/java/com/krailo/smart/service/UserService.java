package com.krailo.smart.service;


import com.krailo.smart.dto.UserDto;
import com.krailo.smart.mapper.UserMapper;
import com.krailo.smart.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private UserMapper userMapper;

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::mapEntityToDto).toList();
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByName(username).map(user -> new User(
                user.getName(),
                user.getPassword(),
                Collections.singleton(user.getRole())
        )).orElseThrow(() -> new UsernameNotFoundException("failed to retrieve user: " + username));


    }
}
