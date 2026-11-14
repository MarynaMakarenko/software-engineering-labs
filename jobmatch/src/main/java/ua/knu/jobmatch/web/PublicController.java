package ua.knu.jobmatch.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import ua.knu.jobmatch.config.AccountPrincipal;
import ua.knu.jobmatch.domain.Role;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.repository.ApplicationRepository;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.service.CompanyService;
import ua.knu.jobmatch.web.form.VacancySearchCriteria;

/** Публічні сторінки: Головна, Вакансії (G1), Вакансія, Допомога (U3), Про агенцію. */
@Controller
public class PublicController {
    private final CompanyService companyService;
    private final PersonRepository people;
    private final ApplicationRepository applications;

    public PublicController(CompanyService companyService, PersonRepository people, ApplicationRepository applications) {
        this.companyService = companyService;
        this.people = people;
        this.applications = applications;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latest", companyService.latestOpen());
        model.addAttribute("openCount", companyService.allOpen().size());
        return "home";
    }

    @GetMapping("/vacancies")
    public String vacancies(@ModelAttribute("criteria") VacancySearchCriteria criteria,
                            org.springframework.validation.BindingResult binding, Model model) {
        SearchErrors.report(binding, model);
        model.addAttribute("vacancies", companyService.searchOpen(criteria));
        model.addAttribute("crumbs", Crumb.of("Вакансії"));
        return "vacancies/list";
    }

    @GetMapping("/vacancies/{id}")
    public String vacancy(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal me, Model model) {
        Vacancy v = companyService.getVacancy(id);
        if (!v.isOpen() && (me == null || me.getRole() == Role.CANDIDATE)) {
            model.addAttribute("closed", true);
        }
        model.addAttribute("vacancy", v);
        if (me != null && me.getRole() == Role.CANDIDATE) {
            people.findByUserId(me.getAccountId()).ifPresent(p -> {
                model.addAttribute("myPersonId", p.getId());
                applications.findByVacancyIdAndPersonId(id, p.getId()).ifPresent(a -> model.addAttribute("myApplication", a));
            });
        }
        model.addAttribute("crumbs", Crumb.of("Вакансії", "/vacancies", v.getPosition()));
        return "vacancies/detail";
    }

    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("crumbs", Crumb.of("Допомога"));
        return "help";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("crumbs", Crumb.of("Про агенцію"));
        return "about";
    }
}
