package com.krailo.smart.report;

import com.krailo.smart.entity.Student;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Виписка учня за період: баланс на початок, операції за датою, підсумки, баланс на кінець.
// Заповнює Report.createBill; кінцевий = початковий + сплачено - нараховано.
@Data
public class Bill {

    private Student student;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal openingBalance = BigDecimal.ZERO;   // на початок дня startDate
    private List<ItemBill> items = new ArrayList<>();
    private BigDecimal totalCharged = BigDecimal.ZERO;     // списано за уроки в періоді
    private BigDecimal totalPaid = BigDecimal.ZERO;        // сплачено в періоді
    private BigDecimal closingBalance = BigDecimal.ZERO;   // на кінець дня endDate
}
