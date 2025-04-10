package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.WebUser;
import com.SpringBootMVC.ExpensesTracker.entity.User;
import com.SpringBootMVC.ExpensesTracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The SignupController handles user registration.
 */
@Controller
public class SignupController {

    private final UserService userService;

    @Autowired
    public SignupController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Initializes data binder to trim input strings.
     *
     * @param dataBinder the WebDataBinder instance for this controller.
     */
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        // Register custom editor for String.class to trim leading/trailing whitespace.
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    /**
     * Displays the registration form page.
     *
     * @param model the model to pass attributes to the view.
     * @return the view name "registration-page".
     */
    @GetMapping("/showRegistrationForm")
    public String showRegistrationForm(Model model) {
        model.addAttribute("webUser", new WebUser());
        return "registration-page";
    }

    /**
     * Processes user registration form submission.
     *
     * @param webUser the WebUser DTO annotated for validation.
     * @param result  binding result containing validation errors (if any).
     * @param model   the model to pass attributes to the view.
     * @return a redirect string depending on the validation and processing outcome.
     */
    @PostMapping("/processRegistration")
    public String processRegistration(@Valid @ModelAttribute("webUser") WebUser webUser,
                                      BindingResult result,
                                      Model model) {
        // Validate form input.
        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.toString());
            return "registration-page";
        }

        // Check for duplicate username.
        String username = webUser.getUsername();
        User existingUser = userService.findUserByUserName(username);
        if (existingUser != null) {
            // Redirect back to registration form with parameter indicating user is already found.
            model.addAttribute("webUser", new WebUser());
            return "redirect:/showRegistrationForm?userFound";
        }

        // Save the new user.
        userService.save(webUser);
        return "redirect:/showLoginPage?registrationSuccess";
    }
}
