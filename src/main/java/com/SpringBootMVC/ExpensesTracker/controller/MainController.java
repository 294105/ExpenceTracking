package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.service.CategoryService;
import com.SpringBootMVC.ExpensesTracker.service.ExpenseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MainController manages expense tracking functionalities such as adding,
 * updating, deleting, and filtering expenses.
 */
@Controller
public class MainController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    @Autowired
    public MainController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    /**
     * Displays the landing page.
     *
     * @param session HTTP session containing user data.
     * @param model   Spring model to add attributes.
     * @return the landing page view name.
     */
    @GetMapping("/")
    public String landingPage(HttpSession session, Model model) {
        Client client = (Client) session.getAttribute("client");
        model.addAttribute("sessionClient", client);
        return "landing-page";
    }

    /**
     * Displays the form to add a new expense.
     *
     * @param model Spring model to add attributes.
     * @return the add expense view.
     */
    @GetMapping("/showAdd")
    public String addExpense(Model model) {
        model.addAttribute("expense", new ExpenseDTO());
        return "add-expense";
    }

    /**
     * Processes new expense submission.
     *
     * @param expenseDTO Expense data transfer object populated from the form.
     * @param session    HTTP session containing client details.
     * @return a redirect to the expense list view.
     */
    @PostMapping("/submitAdd")
    public String submitAdd(@ModelAttribute("expense") ExpenseDTO expenseDTO, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        expenseDTO.setClientId(client.getId());
        expenseService.save(expenseDTO);
        return "redirect:/list";
    }

    /**
     * Lists all expenses for the logged-in client.
     *
     * @param model   Spring model to add attributes.
     * @param session HTTP session containing client details.
     * @return the list-page view.
     */
    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        int clientId = client.getId();
        List<Expense> expenseList = expenseService.findAllExpensesByClientId(clientId);

        // Format each expense with category name, date, and time.
        for (Expense expense : expenseList) {
            expense.setCategoryName(categoryService.findCategoryById(
                    expense.getCategory().getId()).getName());
            LocalDateTime dateTime = LocalDateTime.parse(expense.getDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            expense.setDate(dateTime.toLocalDate().toString());
            expense.setTime(dateTime.toLocalTime().toString());
        }
        model.addAttribute("expenseList", expenseList);
        model.addAttribute("filter", new FilterDTO());
        return "list-page";
    }

    /**
     * Displays the update form pre-populated with expense details.
     *
     * @param id    the id of the expense to update.
     * @param model Spring model to add attributes.
     * @return the update-page view.
     */
    @GetMapping("/showUpdate")
    public String showUpdate(@RequestParam("expId") int id, Model model) {
        Expense expense = expenseService.findExpenseById(id);

        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setCategory(expense.getCategory().getName());
        expenseDTO.setDescription(expense.getDescription());
        expenseDTO.setDateTime(expense.getDateTime());

        model.addAttribute("expense", expenseDTO);
        model.addAttribute("expenseId", id);
        return "update-page";
    }

    /**
     * Processes the submission of an expense update.
     *
     * @param id         the id of the expense being updated.
     * @param expenseDTO Expense data transfer object with updated values.
     * @param session    HTTP session containing client details.
     * @return a redirect to the expense list view.
     */
    @PostMapping("/submitUpdate")
    public String update(@RequestParam("expId") int id,
                         @ModelAttribute("expense") ExpenseDTO expenseDTO,
                         HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        expenseDTO.setExpenseId(id);
        expenseDTO.setClientId(client.getId());
        expenseService.update(expenseDTO);
        return "redirect:/list";
    }

    /**
     * Deletes an expense based on its id.
     *
     * @param id the id of the expense to be deleted.
     * @return a redirect to the expense list view.
     */
    @GetMapping("/delete")
    public String delete(@RequestParam("expId") int id) {
        expenseService.deleteExpenseById(id);
        return "redirect:/list";
    }

    /**
     * Processes expense filtering based on provided filter criteria.
     *
     * @param filter DTO containing filter criteria.
     * @param model  Spring model to add attributes.
     * @return the filter-result view showing filtered expenses.
     */
    @PostMapping("/processFilter")
    public String processFilter(@ModelAttribute("filter") FilterDTO filter, Model model) {
        System.out.println("--------------------------------------------------------------");
        System.out.println("Filter values: " + filter);

        List<Expense> expenseList = expenseService.findFilterResult(filter);
        System.out.println("Number of expenses found: " + expenseList.size());
        System.out.println(expenseList);

        // Format each expense with category name, date, and time.
        for (Expense expense : expenseList) {
            expense.setCategoryName(categoryService.findCategoryById(
                    expense.getCategory().getId()).getName());
            LocalDateTime dateTime = LocalDateTime.parse(expense.getDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            expense.setDate(dateTime.toLocalDate().toString());
            expense.setTime(dateTime.toLocalTime().toString());
        }
        model.addAttribute("expenseList", expenseList);
        return "filter-result";
    }
}
