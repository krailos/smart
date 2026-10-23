package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.ScheduleConflictException;
import com.krailo.smart.mapper.LessonMapper;
import com.krailo.smart.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonMapper lessonMapper;
    @Mock
    private GangService gangService;

    @InjectMocks
    private LessonService lessonService;

    private final LocalDate date = LocalDate.of(2026, 10, 23);
    private final LocalTime start = LocalTime.of(15, 0);
    private final LocalTime end = LocalTime.of(16, 0);

    private Gang gang; // група 10, основний вчитель id=1

    @BeforeEach
    void setUp() {
        gang = Gang.builder().id(10).name("Англ-1").teacher(Teacher.builder().id(1).build()).build();
    }

    @Test
    void shouldCreateLesson_WithSelectedSubstituteTeacher() {
        // Given: урок провела вчителька-заміна id=2
        LessonDto dto = lessonDto(2);
        stubSuccessfulSave(dto);

        // When
        LessonDto result = lessonService.create(dto);

        // Then: вибраного вчителя не підмінили вчителем групи
        assertThat(result.getId()).isEqualTo(5);
        assertThat(dto.getTeacherId()).isEqualTo(2);
        verify(gangService, never()).findByIdEntity(any());
    }

    @Test
    void shouldUseGangTeacher_WhenTeacherNotSelected() {
        // Given: вчителя не вибрали — урок проводить основний вчитель групи (id=1)
        LessonDto dto = lessonDto(null);
        when(gangService.findByIdEntity(10)).thenReturn(gang);
        stubSuccessfulSave(dto);

        // When
        lessonService.create(dto);

        // Then
        assertThat(dto.getTeacherId()).isEqualTo(1);
    }

    @Test
    void shouldThrowException_WhenEndTimeIsNotAfterStartTime() {
        // Given
        LessonDto dto = lessonDto(2);
        dto.setEndTime(LocalTime.of(14, 0));

        // When & Then
        assertThatThrownBy(() -> lessonService.create(dto))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Час завершення");

        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowException_WhenDateIsMissing() {
        // Given
        LessonDto dto = lessonDto(2);
        dto.setDate(null);

        // When & Then
        assertThatThrownBy(() -> lessonService.create(dto))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("дату");

        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldNotUpdateLesson_WhenEndTimeIsNotAfterStartTime() {
        // Given: при редагуванні переплутали час
        LessonDto dto = lessonDto(2);
        dto.setStartTime(LocalTime.of(17, 0));

        // When & Then
        assertThatThrownBy(() -> lessonService.update(5, dto))
                .isInstanceOf(ScheduleConflictException.class);

        verify(lessonRepository, never()).saveAndFlush(any());
    }

    // успішне збереження: мапер і saveAndFlush повертають урок з id=5
    private void stubSuccessfulSave(LessonDto dto) {
        Lesson saved = Lesson.builder().id(5).build();
        when(lessonMapper.mapDtoToEntityForCreate(dto)).thenReturn(saved);
        when(lessonRepository.saveAndFlush(saved)).thenReturn(saved);
        when(lessonMapper.mapEntityToDto(saved)).thenReturn(LessonDto.builder().id(5).build());
    }

    // урок групи 10, 23.10.2026 15:00–16:00
    private LessonDto lessonDto(Integer teacherId) {
        return LessonDto.builder()
                .subjectId(1)
                .gangId(gang.getId())
                .audienceId(1)
                .teacherId(teacherId)
                .date(date)
                .startTime(start)
                .endTime(end)
                .build();
    }
}
