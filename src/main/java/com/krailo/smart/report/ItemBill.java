package com.krailo.smart.report;

import com.krailo.smart.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Component
public class ItemBill {

    private Student student;
    private Discount discount;
    private Lesson lesson;
    private Subject subject;
    private Price price;
    private LocalDate date;
    private Gang gang;
    private LessonsStudents lessonStudent;
    private int itemSum;

    public ItemBill() {

    }
    
    public void setItemSum () {
        this.itemSum = calculateItemSum();
    }

    private int calculateItemSum() {
        if (lessonStudent.isPresent() == false) {
            return this.itemSum = 0;
        }
        if (lessonStudent.isPayed() == false) {
            return this.itemSum = 0;
        }
        if (discount == null) {
            return price.getValue();
        } else {
            itemSum = price.getValue();
            return itemSum - itemSum * discount.getValue() / 100;
        }
    }

}
