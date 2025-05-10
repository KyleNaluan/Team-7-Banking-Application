package com.team7.bankingapp.service;

import com.team7.bankingapp.model.*;
import com.team7.bankingapp.repository.CategoryBudgetRepository;
import com.team7.bankingapp.repository.CustomerRepository;
import com.team7.bankingapp.repository.TotalBudgetRepository;

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
class CategoryBudgetServiceTest {

    @Mock
    private CategoryBudgetRepository categoryBudgetRepo;

    @Mock
    private TotalBudgetRepository totalBudgetRepo;

    @Mock
    private CustomerRepository customerRepo;

    @InjectMocks
    private CategoryBudgetService categoryBudgetService;

    private Customer customer;
    private Category category;
    private TotalBudget totalBudget;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setCustomerID(1L);

        category = new Category();
        category.setCategoryID(10);
        category.setCategoryName("Groceries");

        totalBudget = new TotalBudget(customer, LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                new BigDecimal("500.00"));
    }

    @Test
    void testSetOrUpdateCategoryBudget_successfulCreate() {
        BigDecimal amount = new BigDecimal("100.00");

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(totalBudget));
        when(categoryBudgetRepo.findByCustomerCustomerIDAndCategoryCategoryIDAndMonthAndYear(anyLong(), anyInt(),
                anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(categoryBudgetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryBudget result = categoryBudgetService.setOrUpdateCategoryBudget(customer, category, amount);

        assertEquals(customer, result.getCustomer());
        assertEquals(category, result.getCategory());
        assertEquals(amount, result.getMonthlyLimit());
    }

    @Test
    void testSetOrUpdateCategoryBudget_updatesExisting() {
        BigDecimal amount = new BigDecimal("150.00");

        CategoryBudget existing = new CategoryBudget(customer, category, LocalDate.now().getMonthValue(),
                LocalDate.now().getYear(), new BigDecimal("120.00"));

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(totalBudget));
        when(categoryBudgetRepo.findByCustomerCustomerIDAndCategoryCategoryIDAndMonthAndYear(anyLong(), anyInt(),
                anyInt(), anyInt()))
                .thenReturn(Optional.of(existing));
        when(categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(existing));
        when(categoryBudgetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryBudget result = categoryBudgetService.setOrUpdateCategoryBudget(customer, category, amount);

        assertEquals(amount, result.getMonthlyLimit());
    }

    @Test
    void testSetOrUpdateCategoryBudget_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> categoryBudgetService.setOrUpdateCategoryBudget(customer, category, new BigDecimal("-30.00")));
    }

    @Test
    void testSetOrUpdateCategoryBudget_customerNotFound_throwsException() {
        when(customerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> categoryBudgetService.setOrUpdateCategoryBudget(customer, category, new BigDecimal("100.00")));
    }

    @Test
    void testSetOrUpdateCategoryBudget_totalBudgetNotSet_throwsException() {
        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> categoryBudgetService.setOrUpdateCategoryBudget(customer, category, new BigDecimal("100.00")));
    }

    @Test
    void testSetOrUpdateCategoryBudget_exceedsTotalBudget_throwsException() {
        BigDecimal amount = new BigDecimal("400.00");

        CategoryBudget existing = new CategoryBudget(customer, new Category(), LocalDate.now().getMonthValue(),
                LocalDate.now().getYear(), new BigDecimal("200.00"));

        when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(Optional.of(totalBudget));
        when(categoryBudgetRepo.findByCustomerCustomerIDAndCategoryCategoryIDAndMonthAndYear(anyLong(), anyInt(),
                anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> categoryBudgetService.setOrUpdateCategoryBudget(customer, category, amount));
    }
}
