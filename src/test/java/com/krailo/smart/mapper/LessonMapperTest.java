package com.krailo.smart.mapper;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.service.GangService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonMapperTest {

    @Mock
    private GangService gangService;

    @InjectMocks
    private LessonMapper lessonMapper;

    private Teacher gangTeacher;      // основний вчитель групи
    private Gang gang;

    @BeforeEach
    void setUp() {
        gangTeacher = Teacher.builder().id(1).lastName("Основна").build();
        gang = Gang.builder().id(10).teacher(gangTeacher).build();
    }

    @Test
    void shouldSaveSubstituteTeacher_WhenOtherTeacherSelected() {
        // Given: урок групи 10 провела вчителька-заміна id=2
        when(gangService.findByIdEntity(10)).thenReturn(gang);
        LessonDto dto = lessonDto(2);

        // When
        Lesson lesson = lessonMapper.mapDtoToEntityForCreate(dto);

        // Then: в уроці записано того, хто ФАКТИЧНО провів урок, і слот розкладу, з якого він походить
        assertThat(lesson.getTeacher().getId()).isEqualTo(2);
        assertThat(lesson.getSchedule().getId()).isEqualTo(3);
    }

    @Test
    void shouldUseGangTeacher_WhenTeacherNotSelected() {
        // Given
        when(gangService.findByIdEntity(10)).thenReturn(gang);

        // When
        Lesson lesson = lessonMapper.mapDtoToEntityForCreate(lessonDto(null));

        // Then
        assertThat(lesson.getTeacher()).isEqualTo(gangTeacher);
    }

    @Test
    void shouldKeepLessonTeacher_WhenUpdatingWithoutTeacher() {
        // Given: урок провела заміна (id=2); редагуємо урок, але вчителя у формі не передали
        Teacher substitute = Teacher.builder().id(2).build();
        Lesson lesson = Lesson.builder().id(5).teacher(substitute).build();

        // When
        lessonMapper.mapDtoToEntityForUpdate(lessonDto(null), lesson);

        // Then: історія не переписана — урок і далі за вчителькою-заміною
        assertThat(lesson.getTeacher()).isEqualTo(substitute);
    }

    @Test
    void shouldChangeDateButNotTime_WhenUpdating() {
        // Given: урок 23.10 12:00–13:00; у формі змінили дату на 24.10,
        // а час з форми не приходить (поля disabled) — у DTO його немає
        Lesson lesson = Lesson.builder().id(5)
                .date(LocalDate.of(2026, 10, 23))
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .build();
        LessonDto dto = lessonDto(2);
        dto.setDate(LocalDate.of(2026, 10, 24));
        dto.setStartTime(null);
        dto.setEndTime(null);

        // When
        lessonMapper.mapDtoToEntityForUpdate(dto, lesson);

        // Then: дату змінено, час лишився як був
        assertThat(lesson.getDate()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(lesson.getStartTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(lesson.getEndTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(lesson.getTeacher().getId()).isEqualTo(2);
    }

    private LessonDto lessonDto(Integer teacherId) {
        return LessonDto.builder()
                .scheduleId(3)
                .subjectId(1)
                .gangId(gang.getId())
                .audienceId(1)
                .teacherId(teacherId)
                .date(LocalDate.of(2026, 10, 23))
                .build();
    }
}
