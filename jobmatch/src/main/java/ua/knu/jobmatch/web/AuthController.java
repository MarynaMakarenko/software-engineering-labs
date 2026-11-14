package ua.knu.jobmatch.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.knu.jobmatch.config.AccountPrincipal;
import ua.knu.jobmatch.domain.Role;
import ua.knu.jobmatch.service.AccountService;
import ua.knu.jobmatch.service.BusinessException;
import ua.knu.jobmatch.web.form.RegistrationForm;

/** Вхід (U1) і реєстрація (G2). */
@Controller
public class AuthController {
    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AccountPrincipal me, Model model) {
        if (me != null) {
            return "redirect:" + me.homeUrl();
        }
        model.addAttribute("crumbs", Crumb.of("Вхід"));
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        model.addAttribute("crumbs", Crumb.of("Реєстрація"));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result,
                           HttpServletRequest request, Model model, RedirectAttributes ra) throws ServletException {
        model.addAttribute("crumbs", Crumb.of("Реєстрація"));
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            accountService.register(form);
        } catch (BusinessException e) {
            model.addAttribute("error", e);
            return "auth/register";
        }
        ra.addFlashAttribute("success", "Реєстрацію завершено. Увійдіть з указаними e-mail і паролем.");
        return "redirect:/login";
    }
}
