package com.krailo.smart.report;

import com.krailo.smart.entity.TransactionStudent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Рядок виписки учня: одна транзакція з журналу (урок або оплата) і баланс учня ПІСЛЯ неї.
// Нічого не перераховує: ціна, знижка й сума — ті, що реально записали при проведенні уроку чи оплати.
@Data
@AllArgsConstructor
public class ItemBill {

    private TransactionStudent transaction;
    private BigDecimal balance;

    public boolean isPayment() {
        return transaction.getPayment() != null;
    }

    // початковий баланс учня (або його сторно) — не гроші, отримані в періоді
    public boolean isOpeningBalance() {
        return isPayment() && transaction.getPayment().isOpeningBalance();
    }

    // дата операції: для оплати — дата оплати, для уроку — дата уроку
    public LocalDate getDate() {
        return isPayment() ? transaction.getPayment().getDate() : transaction.getLesson().getDate();
    }

    // як рядок змінює баланс: оплата додає credit, урок віднімає
    // (у сторно credit від'ємний, тому знаки самі стають протилежними)
    public BigDecimal getAmount() {
        return isPayment() ? transaction.getCredit() : transaction.getCredit().negate();
    }
}
