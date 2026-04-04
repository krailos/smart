package com.krailo.smart.report;

import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Data
@AllArgsConstructor
@Component
public class Bill {
    
    private Student student;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ItemBill> items;
    private List<Payment> payments;
    int totalItemSum;
    int totalPaymentSum;
    int balance;

    public Bill () {
        
    }
    
    public void calculateTotalItemSum() {
        for (ItemBill i : items) {
            totalItemSum = i.getItemSum() + totalItemSum;
        }
    }

    public void calculateTotalPaymentSum() {
        for (Payment p : payments) {
            totalPaymentSum = p.getValue() + totalPaymentSum;
        }
    }

    public void calculeateBalance() {
        balance = totalPaymentSum - totalItemSum;     
    }

}
