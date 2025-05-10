package com.team7.bankingapp.model;

import java.io.Serializable;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TotalBudgetId implements Serializable {
    private long customer;
    private int month;
    private int year;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TotalBudgetId))
            return false;
        TotalBudgetId that = (TotalBudgetId) o;
        return customer == that.customer && month == that.month && year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, month, year);
    }
}
