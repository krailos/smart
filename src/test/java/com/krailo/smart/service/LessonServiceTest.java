package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.LessonException;
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
import java.util.Optional;

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
    void shouldThrowException_WhenLessonFromThisScheduleAlreadyHeldOnThisDate() {
        // Given: урок за розкладом id=3 на 23.10 вже проведено (повторне натискання / F5)
        LessonDto dto = lessonDto(2);
        when(lessonRepository.existsByScheduleIdAndDate(3, date)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> lessonService.create(dto))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("уже проведено");

        // урок не збережено — з учнів не спишуть двічі
        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowException_WhenLessonIsNotFromSchedule() {
        // Given: немає слоту розкладу
        LessonDto dto = lessonDto(2);
        dto.setScheduleId(null);

        // When & Then
        assertThatThrownBy(() -> lessonService.create(dto))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("тільки з розкладу");

        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowException_WhenUpdatingDateToDayWithLessonFromSameSchedule() {
        // Given: урок id=5 за розкладом id=3; переносимо на 23.10, а на цю дату з цього розкладу вже є інший урок
        Lesson lesson = Lesson.builder().id(5).schedule(Schedule.builder().id(3).build()).build();
        when(lessonRepository.findById(5)).thenReturn(Optional.of(lesson));
        when(lessonRepository.existsByScheduleIdAndDateAndIdNot(3, date, 5)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> lessonService.update(5, lessonDto(2)))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("уже проведено");

        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldUpdateLessonDate_WhenNoOtherLessonOnThatDate() {
        // Given
        Lesson lesson = Lesson.builder().id(5).schedule(Schedule.builder().id(3).build()).build();
        when(lessonRepository.findById(5)).thenReturn(Optional.of(lesson));
        when(lessonRepository.existsByScheduleIdAndDateAndIdNot(3, date, 5)).thenReturn(false);
        when(lessonMapper.mapDtoToEntityForUpdate(any(LessonDto.class), eq(lesson))).thenReturn(lesson);
        when(lessonRepository.saveAndFlush(lesson)).thenReturn(lesson);
        when(lessonMapper.mapEntityToDto(lesson)).thenReturn(LessonDto.builder().id(5).build());

        // When
        LessonDto result = lessonService.update(5, lessonDto(2));

        // Then
        assertThat(result.getId()).isEqualTo(5);
        verify(lessonRepository).saveAndFlush(lesson);
    }

    @Test
    void shouldThrowException_WhenUpdatingWithoutDate() {
        // Given: дату у формі стерли
        Lesson lesson = Lesson.builder().id(5).build();
        when(lessonRepository.findById(5)).thenReturn(Optional.of(lesson));
        LessonDto dto = lessonDto(2);
        dto.setDate(null);

        // When & Then
        assertThatThrownBy(() -> lessonService.update(5, dto))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("дату");

        verify(lessonRepository, never()).saveAndFlush(any());
    }

    // успішне збереження: мапер і saveAndFlush повертають урок з id=5
    @Test
    void shouldThrowException_WhenNoTeacherSelectedAndGangHasNoTeacher() {
        // Given: вчителя у формі не вибрали, а в групи немає основного вчителя — підставити нікого
        Gang gangWithoutTeacher = Gang.builder().id(10).name("Англ-1").build();
        when(gangService.findByIdEntity(10)).thenReturn(gangWithoutTeacher);

        // When & Then: урок без вчителя не проводимо
        assertThatThrownBy(() -> lessonService.create(lessonDto(null)))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Оберіть вчителя уроку");
        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldNotUpdateStornedLesson() {
        // Given: урок сторновано (гроші повернуто) — він лишається в історії лише для перегляду
        Lesson storned = Lesson.builder().id(5).storned(true).build();
        when(lessonRepository.findById(5)).thenReturn(Optional.of(storned));

        // When & Then: змінити дату чи вчителя такого уроку не можна — тільки провести заново з розкладу
        assertThatThrownBy(() -> lessonService.update(5, lessonDto(2)))
                .isInstanceOf(LessonException.class)
                .hasMessageContaining("Сторнований урок не редагується");
        verify(lessonRepository, never()).saveAndFlush(any());
    }

    private void stubSuccessfulSave(LessonDto dto) {
        Lesson saved = Lesson.builder().id(5).build();
        when(lessonMapper.mapDtoToEntityForCreate(dto)).thenReturn(saved);
        when(lessonRepository.saveAndFlush(saved)).thenReturn(saved);
        when(lessonMapper.mapEntityToDto(saved)).thenReturn(LessonDto.builder().id(5).build());
    }

    // урок групи 10 за розкладом id=3, 23.10.2026 15:00–16:00
    private LessonDto lessonDto(Integer teacherId) {
        return LessonDto.builder()
                .scheduleId(3)
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
