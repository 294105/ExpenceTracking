package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ExpenseServiceImpl implements business logic for managing expenses.
 */
@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ClientService clientService;
    private final CategoryService categoryService;
    private final EntityManager entityManager;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                              ClientService clientService,
                              CategoryService categoryService,
                              EntityManager entityManager) {
        this.expenseRepository = expenseRepository;
        this.clientService = clientService;
        this.categoryService = categoryService;
        this.entityManager = entityManager;
    }

    /**
     * Finds an expense by its unique identifier.
     *
     * @param id the unique identifier of the expense.
     * @return the Expense if found, or null if not found.
     */
    @Override
    public Expense findExpenseById(int id) {
        return expenseRepository.findById(id).orElse(null);
    }

    /**
     * Persists a new expense using the provided ExpenseDTO.
     *
     * @param expenseDTO the DTO containing expense data.
     */
    @Transactional
    @Override
    public void save(ExpenseDTO expenseDTO) {
        System.out.println("Saving expense: " + expenseDTO);
        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDateTime(expenseDTO.getDateTime());
        expense.setDescription(expenseDTO.getDescription());
        // Set client based on the provided client ID.
        expense.setClient(clientService.findClientById(expenseDTO.getClientId()));
        // Look up category using its name.
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        expense.setCategory(category);
        expenseRepository.save(expense);
    }

    /**
     * Updates an existing expense with new data from the provided DTO.
     *
     * @param expenseDTO the DTO containing updated expense data.
     */
    @Override
    public void update(ExpenseDTO expenseDTO) {
        Expense existingExpense = expenseRepository.findById(expenseDTO.getExpenseId()).orElse(null);
        if (existingExpense != null) {
            existingExpense.setAmount(expenseDTO.getAmount());
            existingExpense.setDateTime(expenseDTO.getDateTime());
            existingExpense.setDescription(expenseDTO.getDescription());
            // Update category using its name.
            Category updatedCategory = categoryService.findCategoryByName(expenseDTO.getCategory());
            existingExpense.setCategory(updatedCategory);
            expenseRepository.save(existingExpense);
        }
    }

    /**
     * Retrieves all expenses.
     *
     * @return a list of all expenses.
     */
    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * Retrieves all expenses for a specific client.
     *
     * @param id the client's unique identifier.
     * @return a list of expenses for the client.
     */
    @Override
    public List<Expense> findAllExpensesByClientId(int id) {
        return expenseRepository.findByClientId(id);
    }

    /**
     * Deletes an expense based on its unique identifier.
     *
     * @param id the unique identifier of the expense to be deleted.
     */
    @Override
    public void deleteExpenseById(int id) {
        expenseRepository.deleteById(id);
    }

    /**
     * Retrieves filtered expenses based on the provided filter criteria.
     *
     * @param filter the DTO containing filter criteria.
     * @return a list of expenses matching the filter criteria.
     */
    @Override
    public List<Expense> findFilterResult(FilterDTO filter) {
        // Begin building JPQL query with a base string.
        String query = "select e from Expense e where";

        // Append condition for category if it's not set to "all"
        if (!"all".equalsIgnoreCase(filter.getCategory())) {
            String category = filter.getCategory();
            // Retrieve category id for the given category name.
            int categoryId = categoryService.findCategoryByName(category).getId();
            query += String.format(" e.category.id = %d AND", categoryId);
        }
        // Append condition for amount range.
        int from = filter.getFrom();
        int to = filter.getTo();
        query += String.format(" e.amount between %d and %d", from, to);

        // Append condition for year if specified.
        if (!"all".equalsIgnoreCase(filter.getYear())) {
            query += String.format(" AND CAST(SUBSTRING(e.dateTime, 1, 4) AS INTEGER) = %s", filter.getYear());
        }

        // Append condition for month if specified.
        if (!"all".equalsIgnoreCase(filter.getMonth())) {
            query += String.format(" AND CAST(SUBSTRING(e.dateTime, 6, 2) AS INTEGER) = %s", filter.getMonth());
        }

        // Create and execute a typed query using the built JPQL string.
        TypedQuery<Expense> expenseTypedQuery = entityManager.createQuery(query, Expense.class);
        List<Expense> expenseList = expenseTypedQuery.getResultList();

        return expenseList;
    }
}
