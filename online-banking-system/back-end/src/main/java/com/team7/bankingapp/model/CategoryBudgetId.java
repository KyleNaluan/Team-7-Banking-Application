package com.team7.bankingapp.model;

import java.io.Serializable;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBudgetId implements Serializable {
    private long customer;
    private int category;
    private int month;
    private int year;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CategoryBudgetId))
            return false;
        CategoryBudgetId that = (CategoryBudgetId) o;
        return customer == that.customer &&
                category == that.category &&
                month == that.month &&
                year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, category, month, year);
    }
}
