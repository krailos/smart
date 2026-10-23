package com.krailo.smart.service;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.exception.ScheduleConflictException;
import com.krailo.smart.mapper.ScheduleMapper;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository; // імітую репозиторій
    @Mock
    private GangRepository gangRepository;
    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleService scheduleService; // сюди підставляю зімітований репозиторій

    private WeekDay testWeekDay;
    private LocalTime testStartTime;
    private LocalTime testEndTime;
    private Audience testAudience;
    private Teacher testTeacher;
    private Gang testGang;
    private Subject testSubject;
    private Student testStudent;

    // "чужі" ресурси — для заняття, яке вже є в розкладі
    private Audience otherAudience;
    private Teacher otherTeacher;
    private Gang otherGang;


    @BeforeEach
    void setUp() {
        testWeekDay = WeekDay.ВІВТОРОК;
        testStartTime = LocalTime.of(13, 30);
        testEndTime = LocalTime.of(14, 30);
        testAudience = Audience.builder().id(1).name("testAudience").build();
        testTeacher = Teacher.builder().id(1).firstName("testTeacher").build();
        testSubject = Subject.builder().id(1).name("testSubject").build();
        testStudent = Student.builder().id(1).lastName("Петренко").build();
        testGang = gang(1, "testGang", testTeacher, testStudent);

        otherAudience = Audience.builder().id(2).name("otherAudience").build();
        otherTeacher = Teacher.builder().id(2).firstName("otherTeacher").build();
        otherGang = gang(2, "otherGang", otherTeacher, Student.builder().id(2).lastName("Коваль").build());
    }


    @Test
    void shouldCreateSchedule_WhenNoConflictsExist() {
        // Given
        Schedule newSchedule = Schedule.builder()
                .weekDay(testWeekDay)
                .gang(testGang)
                .audience(testAudience)
                .subject(testSubject)
                .startTime(testStartTime)
                .endTime(testEndTime)
                .build();

        ScheduleDto newScheduleDto = ScheduleDto.builder().build();

        // Налаштовуємо мок: у цей час база даних повертає ПУСТИЙ список (нікого немає)
        when(gangRepository.findById(1)).thenReturn(Optional.of(testGang));
        when(scheduleRepository.findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 0))
                .thenReturn(Collections.emptyList());

        when(scheduleMapper.mapDtoToEntityForCreate(any(ScheduleDto.class))).thenReturn(newSchedule);
        newScheduleDto.setId(10);
        when(scheduleMapper.mapEntityToDto(any(Schedule.class))).thenReturn(newScheduleDto);

        // Коли викликається save, повертаємо цей же об'єкт, але ніби вже з ID
        newSchedule.setId(10);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(newSchedule);

        // When
        ScheduleDto saved = scheduleService.create(newDto());

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10);

        // Перевіряємо, що метод save дійсно викликався 1 раз
        verify(scheduleRepository, times(1)).save(newSchedule);
    }

    @Test
    void shouldCreateSchedule_WhenOverlappingScheduleUsesOtherResources() {
        // Given: у цей час уже є заняття, але інший кабінет, інший вчитель, інша група, інші учні
        when(gangRepository.findById(1)).thenReturn(Optional.of(testGang));
        when(scheduleRepository.findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 0))
                .thenReturn(List.of(existing(otherAudience, otherGang)));
        Schedule saved = Schedule.builder().id(11).build();
        when(scheduleMapper.mapDtoToEntityForCreate(any(ScheduleDto.class))).thenReturn(saved);
        when(scheduleRepository.save(saved)).thenReturn(saved);
        when(scheduleMapper.mapEntityToDto(saved)).thenReturn(ScheduleDto.builder().id(11).build());

        // When
        ScheduleDto result = scheduleService.create(newDto());

        // Then
        assertThat(result.getId()).isEqualTo(11);
        verify(scheduleRepository).save(saved);
    }

    @Test
    void shouldThrowException_WhenAudienceIsOccupied() {
        // Given: той самий кабінет (id=1), але інший вчитель і група
        when(gangRepository.findById(1)).thenReturn(Optional.of(testGang));
        when(scheduleRepository.findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 0))
                .thenReturn(List.of(existing(testAudience, otherGang)));

        // When & Then
        assertThatThrownBy(() -> scheduleService.create(newDto()))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Кабінет");

        // Важливо: перевіряємо, що save НІКОЛИ не викликався, бо валідація зафейлилась
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenTeacherIsBusy() {
        // Given: інший кабінет та група, але вчитель той самий (id=1)
        Gang gangWithSameTeacher = gang(3, "gang3", testTeacher);
        when(gangRepository.findById(1)).thenReturn(Optional.of(testGang));
        when(scheduleRepository.findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 0))
                .thenReturn(List.of(existing(otherAudience, gangWithSameTeacher)));

        // When & Then
        assertThatThrownBy(() -> scheduleService.create(newDto()))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Вчитель");

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenGangIsBusy() {
        // Given: та сама група в іншому кабінеті (вчителя в існуючого заняття немає)
        Schedule sameGang = existing(otherAudience, testGang);
        sameGang.setTeacher(null);
        when(gangRepository.findById(1)).thenReturn(Optional.of(testGang));
        when(scheduleRepository.findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 0))
                .thenReturn(List.of(sameGang));

        // When & Then
        assertThatThrownBy(() -> scheduleService.create(newDto()))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Група");

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenStudentIsBusyInAnotherGang() {
        // Given: учень Петренко (id=1) ходить і в нашу групу, і в іншу, яка вже займається в цей час
        Gang gangWithSameStudent = gang(3, "gang3", otherTeacher, testStudent);
        when(gangRepository.findById(1)).thenReturn(Optional.of(testGang));
        when(scheduleRepository.findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 0))
                .thenReturn(List.of(existing(otherAudience, gangWithSameStudent)));

        // When & Then
        assertThatThrownBy(() -> scheduleService.create(newDto()))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Петренко");

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenEndTimeIsNotAfterStartTime() {
        // Given: кінець раніше за початок
        ScheduleDto dto = newDto();
        dto.setEndTime(LocalTime.of(13, 0));

        // When & Then
        assertThatThrownBy(() -> scheduleService.create(dto))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Час завершення");

        verify(scheduleRepository, never()).findOverlappingSchedules(any(), any(), any(), anyInt());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldExcludeItselfFromConflictCheck_WhenUpdating() {
        // Given: редагуємо розклад з id=5 — він не повинен конфліктувати сам із собою
        Schedule entity = Schedule.builder().id(5).build();
        when(gangRepository.findById(1)).thenReturn(Optional.of(testGang));
        when(scheduleRepository.findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 5))
                .thenReturn(Collections.emptyList());
        when(scheduleRepository.findById(5)).thenReturn(Optional.of(entity));
        when(scheduleMapper.mapDtoToEntityForUpdate(any(ScheduleDto.class), eq(entity))).thenReturn(entity);
        when(scheduleRepository.saveAndFlush(entity)).thenReturn(entity);
        when(scheduleMapper.mapEntityToDto(entity)).thenReturn(ScheduleDto.builder().id(5).build());

        // When
        ScheduleDto result = scheduleService.update(5, newDto());

        // Then: у запит передано excludeId = 5
        assertThat(result.getId()).isEqualTo(5);
        verify(scheduleRepository).findOverlappingSchedules(testWeekDay, testStartTime, testEndTime, 5);
    }


    // нове заняття: кабінет 1, група 1 (вчитель 1, учень Петренко), вівторок 13:30–14:30
    private ScheduleDto newDto() {
        return ScheduleDto.builder()
                .audienceId(testAudience.getId())
                .gangId(testGang.getId())
                .subjectId(testSubject.getId())
                .weekDay(testWeekDay)
                .startTime(testStartTime)
                .endTime(testEndTime)
                .build();
    }

    // заняття, яке вже є в розкладі в той самий час
    private Schedule existing(Audience audience, Gang gang) {
        return Schedule.builder()
                .id(99)
                .audience(audience)
                .gang(gang)
                .teacher(gang.getTeacher())
                .weekDay(testWeekDay)
                .startTime(testStartTime)
                .endTime(testEndTime)
                .build();
    }

    private Gang gang(int id, String name, Teacher teacher, Student... students) {
        Gang gang = Gang.builder().id(id).name(name).teacher(teacher).build();
        gang.setGangStudents(Arrays.stream(students)
                .map(s -> GangsStudents.builder().gang(gang).student(s).build())
                .toList());
        return gang;
    }
}
