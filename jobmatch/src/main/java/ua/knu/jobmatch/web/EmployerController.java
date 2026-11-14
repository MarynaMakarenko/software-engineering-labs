package ua.knu.jobmatch.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.knu.jobmatch.config.AccountPrincipal;
import ua.knu.jobmatch.domain.ApplicationStatus;
import ua.knu.jobmatch.domain.Company;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.matching.MatchingService;
import ua.knu.jobmatch.service.ApplicationService;
import ua.knu.jobmatch.service.CompanyService;
import ua.knu.jobmatch.service.NotFoundException;
import ua.knu.jobmatch.web.form.CompanyForm;
import ua.knu.jobmatch.web.form.VacancyForm;

/** Кабінет представника компанії: E1–E4. */
@Controller
@RequestMapping("/company")
public class EmployerController {
    private final CompanyService companyService;
    private final MatchingService matchingService;
    private final ApplicationService applicationService;

    public EmployerController(CompanyService companyService, MatchingService matchingService,
                              ApplicationService applicationService) {
        this.companyService = companyService;
        this.matchingService = matchingService;
        this.applicationService = applicationService;
    }

    private Company myCompany(AccountPrincipal p) {
        return companyService.getByOwner(p.getAccountId());
    }

    /** Вакансія лише своєї компанії; чужа — як неіснуюча (404). */
    private Vacancy myVacancy(AccountPrincipal p, Long id) {
        Vacancy v = companyService.getVacancy(id);
        if (!v.getCompany().getId().equals(myCompany(p).getId())) {
            throw new NotFoundException("Вакансію №" + id + " не знайдено");
        }
        return v;
    }

    @GetMapping
    public String company(@AuthenticationPrincipal AccountPrincipal p, Model model) {
        model.addAttribute("company", myCompany(p));
        model.addAttribute("crumbs", Crumb.of("Моя компанія"));
        return "employer/company";
    }

    @GetMapping("/edit")
    public String edit(@AuthenticationPrincipal AccountPrincipal p, Model model) {
        model.addAttribute("form", CompanyForm.of(myCompany(p)));
        model.addAttribute("action", "/company/edit");
        model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Редагування"));
        return "shared/company-form";
    }

    @PostMapping("/edit")
    public String save(@AuthenticationPrincipal AccountPrincipal p, @Valid @ModelAttribute("form") CompanyForm form,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/company/edit");
            model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Редагування"));
            return "shared/company-form";
        }
        companyService.update(myCompany(p).getId(), form);
        ra.addFlashAttribute("success", "Профіль компанії збережено.");
        return "redirect:/company";
    }

    @GetMapping("/vacancies")
    public String vacancies(@AuthenticationPrincipal AccountPrincipal p, Model model) {
        model.addAttribute("company", myCompany(p));
        model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Вакансії"));
        return "employer/vacancies";
    }

    @GetMapping("/vacancies/new")
    public String newVacancy(Model model) {
        model.addAttribute("form", new VacancyForm());
        model.addAttribute("action", "/company/vacancies/new");
        model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Вакансії", "/company/vacancies", "Нова вакансія"));
        return "shared/vacancy-form";
    }

    @PostMapping("/vacancies/new")
    public String createVacancy(@AuthenticationPrincipal AccountPrincipal p, @Valid @ModelAttribute("form") VacancyForm form,
                                BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/company/vacancies/new");
            model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Вакансії", "/company/vacancies", "Нова вакансія"));
            return "shared/vacancy-form";
        }
        companyService.addVacancy(myCompany(p).getId(), form);
        ra.addFlashAttribute("success", "Вакансію «" + form.getPosition() + "» опубліковано.");
        return "redirect:/company/vacancies";
    }

    @GetMapping("/vacancies/{id}/edit")
    public String editVacancy(@AuthenticationPrincipal AccountPrincipal p, @PathVariable Long id, Model model) {
        model.addAttribute("form", VacancyForm.of(myVacancy(p, id)));
        model.addAttribute("action", "/company/vacancies/" + id + "/edit");
        model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Вакансії", "/company/vacancies", "Редагування вакансії"));
        return "shared/vacancy-form";
    }

    @PostMapping("/vacancies/{id}/edit")
    public String updateVacancy(@AuthenticationPrincipal AccountPrincipal p, @PathVariable Long id,
                                @Valid @ModelAttribute("form") VacancyForm form, BindingResult result,
                                Model model, RedirectAttributes ra) {
        myVacancy(p, id);
        if (result.hasErrors()) {
            model.addAttribute("action", "/company/vacancies/" + id + "/edit");
            model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Вакансії", "/company/vacancies", "Редагування вакансії"));
            return "shared/vacancy-form";
        }
        companyService.updateVacancy(id, form);
        ra.addFlashAttribute("success", "Вакансію збережено.");
        return "redirect:/company/vacancies";
    }

    @PostMapping("/vacancies/{id}/status")
    public String status(@AuthenticationPrincipal AccountPrincipal p, @PathVariable Long id, @RequestParam boolean open,
                         RedirectAttributes ra) {
        myVacancy(p, id);
        companyService.setVacancyOpen(id, open);
        ra.addFlashAttribute("success", open ? "Вакансію відкрито." : "Вакансію закрито.");
        return "redirect:/company/vacancies";
    }

    @PostMapping("/vacancies/{id}/delete")
    public String delete(@AuthenticationPrincipal AccountPrincipal p, @PathVariable Long id, RedirectAttributes ra) {
        myVacancy(p, id);
        companyService.deleteVacancy(id);
        ra.addFlashAttribute("success", "Вакансію видалено.");
        return "redirect:/company/vacancies";
    }

    // E3 відповідні резюме
    @GetMapping("/vacancies/{id}/matches")
    public String matches(@AuthenticationPrincipal AccountPrincipal p, @PathVariable Long id,
                          @RequestParam(defaultValue = "" + MatchingService.DEFAULT_THRESHOLD) String minScore, Model model) {
        Vacancy v = myVacancy(p, id);
        int threshold = MatchThreshold.parse(minScore, model);
        model.addAttribute("vacancy", v);
        model.addAttribute("results", v.isOpen() ? matchingService.findCandidates(v, threshold) : java.util.List.of());
        model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Вакансії", "/company/vacancies", "Відповідні резюме"));
        return "employer/matches";
    }

    // E4 розгляд кандидатур
    @GetMapping("/vacancies/{id}/applications")
    public String applications(@AuthenticationPrincipal AccountPrincipal p, @PathVariable Long id, Model model) {
        model.addAttribute("vacancy", myVacancy(p, id));
        model.addAttribute("applications", applicationService.forVacancy(id));
        model.addAttribute("crumbs", Crumb.of("Моя компанія", "/company", "Вакансії", "/company/vacancies", "Кандидатури"));
        return "employer/applications";
    }

    @PostMapping("/applications/{appId}/review")
    public String review(@AuthenticationPrincipal AccountPrincipal p, @PathVariable Long appId,
                         @RequestParam ApplicationStatus decision, RedirectAttributes ra) {
        Long vacancyId = applicationService.get(appId).getVacancy().getId();
        applicationService.review(appId, myCompany(p).getId(), decision);
        ra.addFlashAttribute("success", decision == ApplicationStatus.INVITED
                ? "Кандидата запрошено на співбесіду. Рекрутер агенції отримає оновлений статус." : "Кандидатуру відхилено.");
        return "redirect:/company/vacancies/" + vacancyId + "/applications";
    }
}
