package ua.knu.jobmatch.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.knu.jobmatch.config.AccountPrincipal;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.matching.MatchingService;
import ua.knu.jobmatch.service.ApplicationService;
import ua.knu.jobmatch.service.PersonService;
import ua.knu.jobmatch.web.form.EducationForm;
import ua.knu.jobmatch.web.form.PersonForm;
import ua.knu.jobmatch.web.form.WorkRecordForm;

/** Особистий кабінет шукача: C1–C5. */
@Controller
@RequestMapping("/me")
public class CandidateController {
    private final PersonService personService;
    private final MatchingService matchingService;
    private final ApplicationService applicationService;

    public CandidateController(PersonService personService, MatchingService matchingService,
                               ApplicationService applicationService) {
        this.personService = personService;
        this.matchingService = matchingService;
        this.applicationService = applicationService;
    }

    private Person me(AccountPrincipal principal) {
        return personService.getByUser(principal.getAccountId());
    }

    // C1 Ведення резюме
    @GetMapping("/resume")
    public String resume(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        Person p = me(principal);
        model.addAttribute("person", p);
        model.addAttribute("educationForm", new EducationForm());
        model.addAttribute("crumbs", Crumb.of("Моє резюме"));
        return "candidate/resume";
    }

    @GetMapping("/resume/edit")
    public String editResume(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        model.addAttribute("form", PersonForm.of(me(principal)));
        model.addAttribute("crumbs", Crumb.of("Моє резюме", "/me/resume", "Редагування"));
        return "candidate/resume-form";
    }

    @PostMapping("/resume/edit")
    public String saveResume(@AuthenticationPrincipal AccountPrincipal principal,
                             @Valid @ModelAttribute("form") PersonForm form, BindingResult result,
                             Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("crumbs", Crumb.of("Моє резюме", "/me/resume", "Редагування"));
            return "candidate/resume-form";
        }
        personService.update(me(principal).getId(), form);
        ra.addFlashAttribute("success", "Резюме збережено.");
        return "redirect:/me/resume";
    }

    @PostMapping("/education")
    public String addEducation(@AuthenticationPrincipal AccountPrincipal principal,
                               @Valid @ModelAttribute("educationForm") EducationForm form, BindingResult result,
                               Model model, RedirectAttributes ra) {
        Person p = me(principal);
        if (result.hasErrors()) {
            model.addAttribute("person", p);
            model.addAttribute("crumbs", Crumb.of("Моє резюме"));
            model.addAttribute("educationOpen", true);
            return "candidate/resume";
        }
        personService.addEducation(p.getId(), form);
        ra.addFlashAttribute("success", "Запис про освіту додано.");
        return "redirect:/me/resume";
    }

    @PostMapping("/education/{id}/delete")
    public String deleteEducation(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id,
                                  RedirectAttributes ra) {
        personService.removeEducation(me(principal).getId(), id);
        ra.addFlashAttribute("success", "Запис про освіту видалено.");
        return "redirect:/me/resume";
    }

    // C2 Ведення історії роботи
    @GetMapping("/work")
    public String work(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        model.addAttribute("person", me(principal));
        model.addAttribute("crumbs", Crumb.of("Історія роботи"));
        return "candidate/work";
    }

    @GetMapping("/work/new")
    public String newWork(Model model) {
        model.addAttribute("form", new WorkRecordForm());
        model.addAttribute("crumbs", Crumb.of("Історія роботи", "/me/work", "Новий запис"));
        model.addAttribute("action", "/me/work/new");
        return "candidate/work-form";
    }

    @PostMapping("/work/new")
    public String createWork(@AuthenticationPrincipal AccountPrincipal principal,
                             @Valid @ModelAttribute("form") WorkRecordForm form, BindingResult result,
                             Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("crumbs", Crumb.of("Історія роботи", "/me/work", "Новий запис"));
            model.addAttribute("action", "/me/work/new");
            return "candidate/work-form";
        }
        personService.addWorkRecord(me(principal).getId(), form);
        ra.addFlashAttribute("success", "Запис історії роботи додано.");
        return "redirect:/me/work";
    }

    @GetMapping("/work/{id}/edit")
    public String editWork(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id, Model model) {
        model.addAttribute("form", WorkRecordForm.of(personService.getWorkRecord(me(principal).getId(), id)));
        model.addAttribute("crumbs", Crumb.of("Історія роботи", "/me/work", "Редагування запису"));
        model.addAttribute("action", "/me/work/" + id + "/edit");
        return "candidate/work-form";
    }

    @PostMapping("/work/{id}/edit")
    public String updateWork(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id,
                             @Valid @ModelAttribute("form") WorkRecordForm form, BindingResult result,
                             Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("crumbs", Crumb.of("Історія роботи", "/me/work", "Редагування запису"));
            model.addAttribute("action", "/me/work/" + id + "/edit");
            return "candidate/work-form";
        }
        personService.updateWorkRecord(me(principal).getId(), id, form);
        ra.addFlashAttribute("success", "Запис історії роботи збережено.");
        return "redirect:/me/work";
    }

    @PostMapping("/work/{id}/delete")
    public String deleteWork(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id,
                             RedirectAttributes ra) {
        personService.deleteWorkRecord(me(principal).getId(), id);
        ra.addFlashAttribute("success", "Запис історії роботи видалено.");
        return "redirect:/me/work";
    }

    // C3 Перегляд відповідних вакансій
    @GetMapping("/matches")
    public String matches(@AuthenticationPrincipal AccountPrincipal principal,
                          @RequestParam(defaultValue = "" + MatchingService.DEFAULT_THRESHOLD) String minScore,
                          Model model) {
        Person p = me(principal);
        int threshold = MatchThreshold.parse(minScore, model);
        model.addAttribute("person", p);
        model.addAttribute("results", matchingService.findVacancies(p, threshold));
        model.addAttribute("applied", applicationService.byVacancyForPerson(p.getId()));
        model.addAttribute("crumbs", Crumb.of("Відповідні вакансії"));
        return "candidate/matches";
    }

    // C4 Відгук на вакансію
    @PostMapping("/vacancies/{vacancyId}/apply")
    public String apply(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long vacancyId,
                        RedirectAttributes ra) {
        applicationService.apply(vacancyId, me(principal).getId());
        ra.addFlashAttribute("success", "Відгук надіслано. Стан відгуку — у розділі «Мої відгуки».");
        return "redirect:/vacancies/" + vacancyId;
    }

    // C5 Перегляд власних відгуків
    @GetMapping("/applications")
    public String applications(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        model.addAttribute("applications", applicationService.forPerson(me(principal).getId()));
        model.addAttribute("crumbs", Crumb.of("Мої відгуки"));
        return "candidate/applications";
    }
}
