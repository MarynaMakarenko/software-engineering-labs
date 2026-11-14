package ua.knu.jobmatch.web;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.knu.jobmatch.domain.*;
import ua.knu.jobmatch.repository.ApplicationRepository;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.repository.VacancyRepository;
import ua.knu.jobmatch.service.ApplicationService;
import ua.knu.jobmatch.service.CompanyService;
import ua.knu.jobmatch.service.PersonService;
import ua.knu.jobmatch.web.form.*;

import java.time.LocalDate;

/** Робоче місце рекрутера: панель, R1, R4, R5, R6, R7. */
@Controller
@RequestMapping("/recruiter")
public class RecruiterController {
    private final PersonService personService;
    private final CompanyService companyService;
    private final ApplicationService applicationService;
    private final PersonRepository people;
    private final VacancyRepository vacancies;
    private final ApplicationRepository applications;

    public RecruiterController(PersonService personService, CompanyService companyService,
                               ApplicationService applicationService, PersonRepository people,
                               VacancyRepository vacancies, ApplicationRepository applications) {
        this.personService = personService;
        this.companyService = companyService;
        this.applicationService = applicationService;
        this.people = people;
        this.vacancies = vacancies;
        this.applications = applications;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("openVacancies", companyService.allOpen());
        model.addAttribute("searchingCount", people.findByJobStatus(JobStatus.SEARCHING).size());
        model.addAttribute("newApplications", applications.countByStatus(ApplicationStatus.NEW));
        model.addAttribute("invitedApplications", applications.countByStatus(ApplicationStatus.INVITED));
        model.addAttribute("crumbs", Crumb.of("Панель рекрутера"));
        return "recruiter/dashboard";
    }

    // ---------- R1 пошук резюме, R5 ведення даних людей, R4 історія ----------

    @GetMapping("/people")
    public String people(@ModelAttribute("criteria") PersonSearchCriteria criteria, BindingResult binding, Model model) {
        SearchErrors.report(binding, model);
        SearchErrors.checkRange(criteria, model);
        model.addAttribute("people", personService.search(criteria));
        model.addAttribute("crumbs", Crumb.of("Резюме"));
        return "recruiter/people";
    }

    @GetMapping("/people/new")
    public String newPerson(Model model) {
        model.addAttribute("form", new PersonForm());
        model.addAttribute("action", "/recruiter/people/new");
        model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", "Нова людина"));
        return "recruiter/person-form";
    }

    @PostMapping("/people/new")
    public String createPerson(@Valid @ModelAttribute("form") PersonForm form, BindingResult result, Model model,
                               RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/recruiter/people/new");
            model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", "Нова людина"));
            return "recruiter/person-form";
        }
        Person p = personService.create(form);
        ra.addFlashAttribute("success", "Резюме " + p.getFullName() + " додано. Додайте освіту та історію роботи.");
        return "redirect:/recruiter/people/" + p.getId();
    }

    @GetMapping("/people/{id}")
    public String person(@PathVariable Long id, Model model) {
        Person p = personService.get(id);
        model.addAttribute("person", p);
        model.addAttribute("applications", applicationService.forPerson(id));
        model.addAttribute("educationForm", new EducationForm());
        model.addAttribute("workForm", new WorkRecordForm());
        model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", p.getFullName()));
        return "recruiter/person";
    }

    @GetMapping("/people/{id}/edit")
    public String editPerson(@PathVariable Long id, Model model) {
        Person p = personService.get(id);
        model.addAttribute("form", PersonForm.of(p));
        model.addAttribute("action", "/recruiter/people/" + id + "/edit");
        model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", p.getFullName(), "/recruiter/people/" + id, "Редагування"));
        return "recruiter/person-form";
    }

    @PostMapping("/people/{id}/edit")
    public String updatePerson(@PathVariable Long id, @Valid @ModelAttribute("form") PersonForm form, BindingResult result,
                               Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/recruiter/people/" + id + "/edit");
            model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", "Редагування"));
            return "recruiter/person-form";
        }
        personService.update(id, form);
        ra.addFlashAttribute("success", "Резюме збережено.");
        return "redirect:/recruiter/people/" + id;
    }

    @PostMapping("/people/{id}/delete")
    public String deletePerson(@PathVariable Long id, RedirectAttributes ra) {
        String name = personService.get(id).getFullName();
        personService.delete(id);
        ra.addFlashAttribute("success", "Резюме " + name + " видалено разом з освітою, історією роботи та кандидатурами.");
        return "redirect:/recruiter/people";
    }

    @PostMapping("/people/{id}/education")
    public String addEducation(@PathVariable Long id, @Valid @ModelAttribute("educationForm") EducationForm form,
                               BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            Person p = personService.get(id);
            model.addAttribute("person", p);
            model.addAttribute("applications", applicationService.forPerson(id));
            model.addAttribute("workForm", new WorkRecordForm());
            model.addAttribute("educationOpen", true);
            model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", p.getFullName()));
            return "recruiter/person";
        }
        personService.addEducation(id, form);
        ra.addFlashAttribute("success", "Запис про освіту додано.");
        return "redirect:/recruiter/people/" + id;
    }

    @PostMapping("/people/{id}/education/{eduId}/delete")
    public String deleteEducation(@PathVariable Long id, @PathVariable Long eduId, RedirectAttributes ra) {
        personService.removeEducation(id, eduId);
        ra.addFlashAttribute("success", "Запис про освіту видалено.");
        return "redirect:/recruiter/people/" + id;
    }

    @PostMapping("/people/{id}/work")
    public String addWork(@PathVariable Long id, @Valid @ModelAttribute("workForm") WorkRecordForm form,
                          BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            Person p = personService.get(id);
            model.addAttribute("person", p);
            model.addAttribute("applications", applicationService.forPerson(id));
            model.addAttribute("educationForm", new EducationForm());
            model.addAttribute("workOpen", true);
            model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", p.getFullName()));
            return "recruiter/person";
        }
        personService.addWorkRecord(id, form);
        ra.addFlashAttribute("success", "Запис історії роботи додано.");
        return "redirect:/recruiter/people/" + id;
    }

    @PostMapping("/people/{id}/work/{recordId}/delete")
    public String deleteWork(@PathVariable Long id, @PathVariable Long recordId, RedirectAttributes ra) {
        personService.deleteWorkRecord(id, recordId);
        ra.addFlashAttribute("success", "Запис історії роботи видалено.");
        return "redirect:/recruiter/people/" + id;
    }

    // ---------- R6 компанії та вакансії ----------

    @GetMapping("/companies")
    public String companies(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("companies", companyService.list(q));
        model.addAttribute("q", q);
        model.addAttribute("crumbs", Crumb.of("Компанії"));
        return "recruiter/companies";
    }

    @GetMapping("/companies/new")
    public String newCompany(Model model) {
        model.addAttribute("form", new CompanyForm());
        model.addAttribute("action", "/recruiter/companies/new");
        model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", "Нова компанія"));
        return "shared/company-form";
    }

    @PostMapping("/companies/new")
    public String createCompany(@Valid @ModelAttribute("form") CompanyForm form, BindingResult result, Model model,
                                RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/recruiter/companies/new");
            model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", "Нова компанія"));
            return "shared/company-form";
        }
        Company c = companyService.create(form);
        ra.addFlashAttribute("success", "Компанію «" + c.getName() + "» додано.");
        return "redirect:/recruiter/companies/" + c.getId();
    }

    @GetMapping("/companies/{id}")
    public String company(@PathVariable Long id, Model model) {
        Company c = companyService.get(id);
        model.addAttribute("company", c);
        model.addAttribute("applicationCount", companyService.applicationCount(c));
        model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", c.getName()));
        return "recruiter/company";
    }

    @GetMapping("/companies/{id}/edit")
    public String editCompany(@PathVariable Long id, Model model) {
        Company c = companyService.get(id);
        model.addAttribute("form", CompanyForm.of(c));
        model.addAttribute("action", "/recruiter/companies/" + id + "/edit");
        model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", c.getName(), "/recruiter/companies/" + id, "Редагування"));
        return "shared/company-form";
    }

    @PostMapping("/companies/{id}/edit")
    public String updateCompany(@PathVariable Long id, @Valid @ModelAttribute("form") CompanyForm form, BindingResult result,
                                Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/recruiter/companies/" + id + "/edit");
            model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", "Редагування"));
            return "shared/company-form";
        }
        companyService.update(id, form);
        ra.addFlashAttribute("success", "Дані компанії збережено.");
        return "redirect:/recruiter/companies/" + id;
    }

    @PostMapping("/companies/{id}/delete")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes ra) {
        String name = companyService.get(id).getName();
        companyService.delete(id);
        ra.addFlashAttribute("success", "Компанію «" + name + "» видалено разом з її вакансіями.");
        return "redirect:/recruiter/companies";
    }

    @GetMapping("/companies/{id}/vacancies/new")
    public String newVacancy(@PathVariable Long id, Model model) {
        Company c = companyService.get(id);
        model.addAttribute("form", new VacancyForm());
        model.addAttribute("action", "/recruiter/companies/" + id + "/vacancies/new");
        model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", c.getName(), "/recruiter/companies/" + id, "Нова вакансія"));
        return "shared/vacancy-form";
    }

    @PostMapping("/companies/{id}/vacancies/new")
    public String createVacancy(@PathVariable Long id, @Valid @ModelAttribute("form") VacancyForm form, BindingResult result,
                                Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("action", "/recruiter/companies/" + id + "/vacancies/new");
            model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", "Нова вакансія"));
            return "shared/vacancy-form";
        }
        companyService.addVacancy(id, form);
        ra.addFlashAttribute("success", "Вакансію «" + form.getPosition() + "» додано.");
        return "redirect:/recruiter/companies/" + id;
    }

    @GetMapping("/vacancies/{id}/edit")
    public String editVacancy(@PathVariable Long id, Model model) {
        Vacancy v = companyService.getVacancy(id);
        model.addAttribute("form", VacancyForm.of(v));
        model.addAttribute("action", "/recruiter/vacancies/" + id + "/edit");
        model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", v.getCompany().getName(),
                "/recruiter/companies/" + v.getCompany().getId(), "Редагування вакансії"));
        return "shared/vacancy-form";
    }

    @PostMapping("/vacancies/{id}/edit")
    public String updateVacancy(@PathVariable Long id, @Valid @ModelAttribute("form") VacancyForm form, BindingResult result,
                                Model model, RedirectAttributes ra) {
        Vacancy v = companyService.getVacancy(id);
        if (result.hasErrors()) {
            model.addAttribute("action", "/recruiter/vacancies/" + id + "/edit");
            model.addAttribute("crumbs", Crumb.of("Компанії", "/recruiter/companies", "Редагування вакансії"));
            return "shared/vacancy-form";
        }
        companyService.updateVacancy(id, form);
        ra.addFlashAttribute("success", "Вакансію збережено.");
        return "redirect:/recruiter/companies/" + v.getCompany().getId();
    }

    @PostMapping("/vacancies/{id}/status")
    public String vacancyStatus(@PathVariable Long id, @RequestParam boolean open, RedirectAttributes ra) {
        Long companyId = companyService.getVacancy(id).getCompany().getId();
        companyService.setVacancyOpen(id, open);
        ra.addFlashAttribute("success", open ? "Вакансію відкрито." : "Вакансію закрито.");
        return "redirect:/recruiter/companies/" + companyId;
    }

    @PostMapping("/vacancies/{id}/delete")
    public String deleteVacancy(@PathVariable Long id, RedirectAttributes ra) {
        Long companyId = companyService.getVacancy(id).getCompany().getId();
        companyService.deleteVacancy(id);
        ra.addFlashAttribute("success", "Вакансію видалено разом з кандидатурами на неї.");
        return "redirect:/recruiter/companies/" + companyId;
    }

    // ---------- Кандидатури, R7 працевлаштування ----------

    @GetMapping("/applications")
    public String applicationsList(@RequestParam(required = false) ApplicationStatus status, Model model) {
        model.addAttribute("applications", applicationService.all(status));
        model.addAttribute("status", status);
        model.addAttribute("crumbs", Crumb.of("Кандидатури"));
        return "recruiter/applications";
    }

    @GetMapping("/applications/{id}/hire")
    public String hireForm(@PathVariable Long id, Model model) {
        model.addAttribute("candidacy", applicationService.get(id));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("crumbs", Crumb.of("Кандидатури", "/recruiter/applications", "Реєстрація працевлаштування"));
        return "recruiter/hire";
    }

    @PostMapping("/applications/{id}/hire")
    public String hire(@PathVariable Long id,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       RedirectAttributes ra) {
        Application a = applicationService.hire(id, startDate);
        ra.addFlashAttribute("success", "Працевлаштування " + a.getPerson().getFullName() + " на вакансію «"
                + a.getVacancy().getPosition() + "» (" + a.getVacancy().getCompany().getName() + ") зареєстровано.");
        return "redirect:/recruiter/applications";
    }
}
