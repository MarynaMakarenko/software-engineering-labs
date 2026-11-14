package ua.knu.jobmatch.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.knu.jobmatch.domain.Application;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.matching.MatchResult;
import ua.knu.jobmatch.matching.MatchingService;
import ua.knu.jobmatch.service.ApplicationService;
import ua.knu.jobmatch.service.CompanyService;
import ua.knu.jobmatch.service.PersonService;
import ua.knu.jobmatch.web.form.ProposeForm;

import java.util.List;

/** R2 «Підбір кандидатів на вакансію» (критичний) і R3 «Підбір вакансій для шукача». */
@Controller
@RequestMapping("/recruiter")
public class RecruiterMatchController {
    private final CompanyService companyService;
    private final PersonService personService;
    private final MatchingService matchingService;
    private final ApplicationService applicationService;

    public RecruiterMatchController(CompanyService companyService, PersonService personService,
                                    MatchingService matchingService, ApplicationService applicationService) {
        this.companyService = companyService;
        this.personService = personService;
        this.matchingService = matchingService;
        this.applicationService = applicationService;
    }

    /** R2, кроки 1–3 (A1, A2, A4). */
    @GetMapping("/vacancies/{id}/matches")
    public String candidates(@PathVariable Long id,
                             @RequestParam(defaultValue = "" + MatchingService.DEFAULT_THRESHOLD) String minScore,
                             @RequestParam(required = false) String prev, Model model) {
        Vacancy v = companyService.getVacancy(id);
        int threshold = MatchThreshold.parse(minScore, prev, model);
        List<MatchResult> results = v.isOpen() ? matchingService.findCandidates(v, threshold) : List.of();
        model.addAttribute("vacancy", v);
        model.addAttribute("results", results);
        model.addAttribute("proposals", applicationService.byPersonForVacancy(id));
        model.addAttribute("partialThreshold", MatchingService.PARTIAL_THRESHOLD);
        model.addAttribute("crumbs", Crumb.of("Панель рекрутера", "/recruiter", "Підбір кандидатів"));
        return "recruiter/candidate-matches";
    }

    /** R2, крок 5: форма пропозиції кандидата. */
    @GetMapping("/vacancies/{id}/propose")
    public String proposeForm(@PathVariable Long id, @RequestParam Long personId,
                              @RequestParam(defaultValue = "" + MatchingService.DEFAULT_THRESHOLD) String minScore, Model model,
                              RedirectAttributes ra) {
        Vacancy v = companyService.getVacancy(id);
        if (!v.isOpen() || applicationService.byPersonForVacancy(id).containsKey(personId)) {
            ra.addFlashAttribute("error", new ua.knu.jobmatch.service.BusinessException(
                    !v.isOpen() ? "Вакансію «" + v.getPosition() + "» закрито — кандидатури не приймаються."
                            : "Кандидатура цієї людини на вакансію «" + v.getPosition() + "» вже існує.",
                    "Поверніться до списку підбору і виберіть іншого кандидата або відкриту вакансію.",
                    "Кнопка «Запропонувати» доступна лише для кандидатів без кандидатури на відкриту вакансію."));
            return "redirect:/recruiter/vacancies/" + id + "/matches?minScore=" + safeScore(minScore);
        }
        ProposeForm form = new ProposeForm();
        form.setPersonId(personId);
        return renderPropose(id, form, minScore, model);
    }

    /** R2, кроки 6–7 (A3, A5). */
    @PostMapping("/vacancies/{id}/propose")
    public String propose(@PathVariable Long id, @Valid @ModelAttribute("form") ProposeForm form, BindingResult result,
                          @RequestParam(defaultValue = "" + MatchingService.DEFAULT_THRESHOLD) String minScore, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return renderPropose(id, form, minScore, model);
        }
        Application a;
        try {
            a = applicationService.propose(id, form.getPersonId(), form.getComment());
        } catch (ua.knu.jobmatch.service.BusinessException e) {
            ra.addFlashAttribute("error", e);
            return "redirect:/recruiter/vacancies/" + id + "/matches?minScore=" + safeScore(minScore);
        }
        ra.addFlashAttribute("success", "Кандидатуру " + a.getPerson().getFullName() + " запропоновано на вакансію «"
                + a.getVacancy().getPosition() + "» (" + a.getVacancy().getCompany().getName() + ").");
        return "redirect:/recruiter/vacancies/" + id + "/matches?minScore=" + safeScore(minScore);
    }

    private String renderPropose(Long id, ProposeForm form, String minScore, Model model) {
        Vacancy v = companyService.getVacancy(id);
        Person p = personService.get(form.getPersonId());
        model.addAttribute("vacancy", v);
        model.addAttribute("person", p);
        model.addAttribute("match", matchingService.evaluate(p, v));
        model.addAttribute("form", form);
        model.addAttribute("minScore", safeScore(minScore));
        model.addAttribute("crumbs", Crumb.of("Панель рекрутера", "/recruiter", "Підбір кандидатів",
                "/recruiter/vacancies/" + id + "/matches", "Пропозиція кандидата"));
        return "recruiter/propose";
    }

    private static int safeScore(String raw) {
        try {
            int v = Integer.parseInt(raw);
            return v < 0 || v > 100 ? MatchingService.DEFAULT_THRESHOLD : v;
        } catch (NumberFormatException e) {
            return MatchingService.DEFAULT_THRESHOLD;
        }
    }

    /** R3 «Підбір вакансій для шукача». */
    @GetMapping("/people/{id}/matches")
    public String vacancies(@PathVariable Long id,
                            @RequestParam(defaultValue = "" + MatchingService.DEFAULT_THRESHOLD) String minScore,
                            Model model) {
        Person p = personService.get(id);
        int threshold = MatchThreshold.parse(minScore, model);
        model.addAttribute("person", p);
        model.addAttribute("results", matchingService.findVacancies(p, threshold));
        model.addAttribute("applied", applicationService.byVacancyForPerson(id));
        model.addAttribute("crumbs", Crumb.of("Резюме", "/recruiter/people", p.getFullName(),
                "/recruiter/people/" + id, "Підбір вакансій"));
        return "recruiter/vacancy-matches";
    }
}
