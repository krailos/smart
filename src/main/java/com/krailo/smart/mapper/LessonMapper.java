package com.krailo.smart.mapper;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.service.AudienceService;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.SubjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LessonMapper implements Mapper<Lesson, LessonDto> {
    
    private SubjectService subjectService;
    private SubjectMapper subjectMapper;
    private GangService gangService;
    private GangMapper gangMapper;
    private AudienceService audienceService;
    private AudienceMapper audienceMapper;
    private LessonsStudentsMapper lessonsStudentsMapper;

    @Override
    public LessonDto mapEntityToDto(Lesson e) {
        return LessonDto.builder()
                .id(e.getId())
                // у старих уроків (до V17) розкладу немає
                .scheduleId(e.getSchedule() == null ? null : e.getSchedule().getId())
                .subjectId(e.getSubject().getId())
                .subjectName(e.getSubject().getName())
                .gangId(e.getGang().getId())
                .gangName(e.getGang().getName())
                .teacherId(e.getTeacher().getId())
                .teacherName(e.getTeacher().getLastName() + " " + e.getTeacher().getFirstName())
                .audienceId(e.getAudience().getId())
                .audienceName(e.getAudience().getName())
                .date(e.getDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .lessonsStudentsDto(e.getLessonsStudents().stream()
                        .map(lessonsStudentsMapper::mapEntityToDto).toList())
                .storned(e.isStorned())
                .build();
    }


    @Override
    public Lesson mapDtoToEntityForCreate(LessonDto d) {
       Lesson e = new Lesson();
        e.setSchedule(Schedule.builder().id(d.getScheduleId()).build());
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        Gang gang = gangService.findByIdEntity(d.getGangId());
        e.setGang(gang);
        // вчитель, який фактично провів урок (вибраний у формі); якщо не вибрано — основний вчитель групи
        e.setTeacher(d.getTeacherId() != null ? Teacher.builder().id(d.getTeacherId()).build() : gang.getTeacher());
        e.setAudience(Audience.builder().id(d.getAudienceId()).build());
        e.setDate(d.getDate());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }


    @Override
    public Lesson mapDtoToEntityForUpdate(LessonDto d, Lesson e) {
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        e.setGang(Gang.builder().id(d.getGangId()).build());
        // змінюємо вчителя тільки якщо його явно вибрали — історію (хто провів урок) не переписуємо
        if (d.getTeacherId() != null) {
            e.setTeacher(Teacher.builder().id(d.getTeacherId()).build());
        }
        e.setAudience(Audience.builder().id(d.getAudienceId()).build());
        e.setDate(d.getDate());
        // час уроку не змінюємо: його задає розклад
        return e;
    }

}
