package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.TotalBudget;
import com.team7.bankingapp.model.CategoryBudget;
import com.team7.bankingapp.repository.TotalBudgetRepository;
import com.team7.bankingapp.repository.CategoryBudgetRepository;
import com.team7.bankingapp.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TotalBudgetServiceTest {

    @Mock
    private TotalBudgetRepository totalBudgetRepo;

    @Mock
    private CategoryBudgetRepository categoryBudgetRepo;

    @Mock
    private CustomerRepository customerRepo;

    @InjectMocks
    private TotalBudgetService totalBudgetService;

    private Customer customer;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setCustomerID(1L);
    }

    @Test
    void testSetOrUpdateTotalBudget_successfulCreate() {
        BigDecimal amount = new BigDecimal("500.00");

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(totalBudgetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TotalBudget result = totalBudgetService.setOrUpdateTotalBudget(customer, amount);

        assertEquals(customer, result.getCustomer());
        assertEquals(amount, result.getMonthlyTotalLimit());
    }

    @Test
    void testSetOrUpdateTotalBudget_updatesExisting() {
        BigDecimal amount = new BigDecimal("700.00");
        TotalBudget existing = new TotalBudget(customer, LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                new BigDecimal("500.00"));

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(existing));
        when(totalBudgetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TotalBudget result = totalBudgetService.setOrUpdateTotalBudget(customer, amount);

        assertEquals(customer, result.getCustomer());
        assertEquals(amount, result.getMonthlyTotalLimit());
    }

    @Test
    void testSetOrUpdateTotalBudget_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> totalBudgetService.setOrUpdateTotalBudget(customer, new BigDecimal("-100.00")));
    }

    @Test
    void testSetOrUpdateTotalBudget_customerNotFound_throwsException() {
        when(customerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> totalBudgetService.setOrUpdateTotalBudget(customer, new BigDecimal("100.00")));
    }

    @Test
    void testSetOrUpdateTotalBudget_lessThanExistingCategoryBudgets_throwsException() {
        BigDecimal amount = new BigDecimal("100.00");
        CategoryBudget cb1 = new CategoryBudget();
        cb1.setMonthlyLimit(new BigDecimal("60.00"));
        CategoryBudget cb2 = new CategoryBudget();
        cb2.setMonthlyLimit(new BigDecimal("70.00"));

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(cb1, cb2));

        assertThrows(IllegalArgumentException.class, () -> totalBudgetService.setOrUpdateTotalBudget(customer, amount));
    }
}
