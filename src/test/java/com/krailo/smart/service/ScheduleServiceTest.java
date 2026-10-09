package com.krailo.smart.service;

import com.krailo.smart.entity.Audience;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Audience audience;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        weekDay = WeekDay.ВІВТОРОК;
        startTime = LocalTime.of(13, 30);
        endTime = LocalTime.of(14, 30);
        audience = Audience.builder().id(1).name("testAud").build();
        teacher = Teacher.builder().id(1).firstName("testTeacher").build();
    }


}
