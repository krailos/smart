package com.krailo.smart.report;

import com.krailo.smart.entity.Student;
import com.krailo.smart.repository.TransactionStudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

// Звіт учня (виписка) з журналу транзакцій — з того, що реально списали й зарахували,
// а не з перерахунку за поточними цінами і знижками.
@Component
@AllArgsConstructor
public class Report {

    private TransactionStudentRepository transactionStudentRepository;

    public Bill createBill(Student student, LocalDate startDate, LocalDate endDate) {
        Bill bill = new Bill();
        bill.setStudent(student);
        bill.setStartDate(startDate);
        bill.setEndDate(endDate);

        // увесь журнал учня за датою операції; в один день — у порядку внесення (id).
        // Не за готовою колонкою balance: вона в порядку ВНЕСЕННЯ, а урок можуть провести заднім числом
        List<ItemBill> journal = transactionStudentRepository.findAllByStudent(student).stream()
                .map(ts -> new ItemBill(ts, null))
                .sorted(Comparator.comparing(ItemBill::getDate)
                        .thenComparing(item -> item.getTransaction().getId()))
                .toList();

        // 1. початковий баланс — усе, що сталося ДО startDate
        BigDecimal openingBalance = journal.stream()
                .filter(item -> item.getDate().isBefore(startDate))
                .map(ItemBill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setOpeningBalance(openingBalance);

        // 2. операції періоду (межі включно) з балансом після кожної
        BigDecimal balance = openingBalance;
        for (ItemBill item : journal) {
            if (item.getDate().isBefore(startDate) || item.getDate().isAfter(endDate)) {
                continue;
            }
            balance = balance.add(item.getAmount());
            item.setBalance(balance);
            bill.getItems().add(item);
            if (item.isPayment()) {
                bill.setTotalPaid(bill.getTotalPaid().add(item.getAmount()));
            } else {
                bill.setTotalCharged(bill.getTotalCharged().add(item.getTransaction().getCredit()));
            }
        }

        // 3. кінцевий баланс = початковий + сплачено - нараховано
        bill.setClosingBalance(balance);
        return bill;
    }
}
