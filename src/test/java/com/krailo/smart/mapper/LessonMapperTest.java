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

        // Then: в уроці записано того, хто ФАКТИЧНО провів урок
        assertThat(lesson.getTeacher().getId()).isEqualTo(2);
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

    private LessonDto lessonDto(Integer teacherId) {
        return LessonDto.builder()
                .subjectId(1)
                .gangId(gang.getId())
                .audienceId(1)
                .teacherId(teacherId)
                .date(LocalDate.of(2026, 10, 23))
                .build();
    }
}
