package ua.knu.jobmatch.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.knu.jobmatch.domain.*;
import ua.knu.jobmatch.repository.ApplicationRepository;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.repository.VacancyRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Кандидатури: відгук (C4), пропозиція рекрутера (R2), розгляд (E4), працевлаштування (R7). */
@Service
@Transactional
public class ApplicationService {
    private final ApplicationRepository applications;
    private final VacancyRepository vacancies;
    private final PersonRepository people;
    private final Clock clock;

    public ApplicationService(ApplicationRepository applications, VacancyRepository vacancies,
                              PersonRepository people, Clock clock) {
        this.applications = applications;
        this.vacancies = vacancies;
        this.people = people;
        this.clock = clock;
    }

    /** R2, кроки 5–7: рекрутер пропонує кандидата на вакансію. */
    public Application propose(Long vacancyId, Long personId, String comment) {
        return create(vacancyId, personId, ApplicationSource.RECRUITER, comment);
    }

    /** C4: шукач відгукується на вакансію. */
    public Application apply(Long vacancyId, Long personId) {
        return create(vacancyId, personId, ApplicationSource.CANDIDATE, null);
    }

    private Application create(Long vacancyId, Long personId, ApplicationSource source, String comment) {
        Vacancy vacancy = vacancies.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Вакансію №" + vacancyId + " не знайдено"));
        Person person = people.findById(personId)
                .orElseThrow(() -> new NotFoundException("Людину №" + personId + " не знайдено"));
        if (!vacancy.isOpen()) {
            throw new BusinessException("Вакансію «" + vacancy.getPosition() + "» закрито — кандидатури не приймаються.",
                    "Виберіть іншу відкриту вакансію у списку вакансій.",
                    "Перевіряйте статус вакансії «Відкрита» перед поданням кандидатури.");
        }
        if (comment != null && comment.length() > 500) {
            throw new BusinessException("Коментар довший за 500 символів.",
                    "Скоротіть коментар до 500 символів.",
                    "Пишіть 2–3 речення про сильні сторони кандидата.");
        }
        if (applications.existsByVacancyIdAndPersonId(vacancyId, personId)) {
            throw new BusinessException("Кандидатура " + person.getFullName() + " на цю вакансію вже існує.",
                    "Відкрийте розділ «Кандидатури», щоб переглянути її статус.",
                    "Кнопка подання неактивна для вже поданих кандидатур — не відкривайте форму в кількох вкладках.");
        }
        vacancy.getCompany().getName();
        return applications.save(new Application(vacancy, person, source, comment));
    }

    @Transactional(readOnly = true)
    public Map<Long, Application> byPersonForVacancy(Long vacancyId) {
        return applications.findByVacancyIdOrderByCreatedAtDesc(vacancyId).stream()
                .collect(Collectors.toMap(a -> a.getPerson().getId(), Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, Application> byVacancyForPerson(Long personId) {
        return applications.findByPersonIdOrderByCreatedAtDesc(personId).stream()
                .collect(Collectors.toMap(a -> a.getVacancy().getId(), Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<Application> forVacancy(Long vacancyId) {
        List<Application> list = applications.findByVacancyIdOrderByCreatedAtDesc(vacancyId);
        list.forEach(a -> {
            a.getPerson().getWorkRecords().size();
            a.getPerson().getEducations().size();
        });
        return list;
    }

    @Transactional(readOnly = true)
    public List<Application> forPerson(Long personId) {
        return applications.findByPersonIdOrderByCreatedAtDesc(personId);
    }

    @Transactional(readOnly = true)
    public List<Application> all(ApplicationStatus status) {
        return status == null ? applications.findAllByOrderByCreatedAtDesc()
                : applications.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public Application get(Long id) {
        Application a = applications.findById(id).orElseThrow(() -> new NotFoundException("Кандидатуру №" + id + " не знайдено"));
        a.getVacancy().getCompany().getName();
        a.getPerson().getFullName();
        return a;
    }

    /** E4: представник компанії запрошує або відхиляє кандидата (лише своєї компанії). */
    public void review(Long applicationId, Long companyId, ApplicationStatus decision) {
        if (decision != ApplicationStatus.INVITED && decision != ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Unsupported decision " + decision);
        }
        Application a = get(applicationId);
        if (!a.getVacancy().getCompany().getId().equals(companyId)) {
            throw new NotFoundException("Кандидатуру не знайдено");
        }
        if (a.getStatus() != ApplicationStatus.NEW) {
            throw statusChanged(a);
        }
        changeStatus(a, decision);
    }

    /** R7: реєстрація працевлаштування за кандидатурою. */
    public Application hire(Long applicationId, LocalDate startDate) {
        Application a = get(applicationId);
        LocalDate today = LocalDate.now(clock);
        if (a.getStatus() != ApplicationStatus.INVITED) {
            throw new BusinessException("Працевлаштування можна зареєструвати лише для кандидатури зі статусом «Запрошено»; поточний статус — «"
                    + a.getStatus().getLabel() + "».",
                    "Відкрийте «Кандидатури» з фільтром «Запрошено» і виберіть потрібну кандидатуру.",
                    "Реєструйте працевлаштування після того, як компанія запросила кандидата.");
        }
        if (startDate == null || startDate.isAfter(today.plusDays(90))) {
            throw new BusinessException("Дата початку роботи не вказана або пізніша за " + today.plusDays(90).format(DATE) + ".",
                    "Вкажіть дату не пізніше " + today.plusDays(90).format(DATE) + ".",
                    "Реєструйте працевлаштування після підписання договору з датою виходу на роботу.");
        }
        changeStatus(a, ApplicationStatus.HIRED);
        Vacancy v = a.getVacancy();
        a.getPerson().hire(v.getCompany(), v.getPosition(), v.getSalary(), startDate);
        v.close();
        return a;
    }

    private static final java.time.format.DateTimeFormatter DATE = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static BusinessException statusChanged(Application a) {
        return new BusinessException("Статус кандидатури вже змінено: поточний статус — «" + a.getStatus().getLabel() + "».",
                "Перегляньте оновлений рядок кандидатури — рішення для неї вже прийнято.",
                "Оновлюйте сторінку перед прийняттям рішення, якщо вона відкрита давно.");
    }

    private static void changeStatus(Application a, ApplicationStatus status) {
        if (a.getStatus().isFinal()) {
            throw new BusinessException("Кандидатура вже має остаточний статус «" + a.getStatus().getLabel() + "».",
                    "Оновіть сторінку, щоб побачити актуальний статус.",
                    "Остаточні статуси «Відхилено» і «Працевлаштовано» не змінюються.");
        }
        a.changeStatus(status);
    }
}
