package ua.knu.jobmatch.web;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ua.knu.jobmatch.service.BusinessException;

/** Некоректні значення фільтрів пошуку: фільтр ігнорується, актор отримує повідомлення з 3 частин. */
final class SearchErrors {
    private SearchErrors() { }

    /** R1: «зарплата від» більша за «до» — діапазон не застосовується. */
    static void checkRange(ua.knu.jobmatch.web.form.PersonSearchCriteria c, Model model) {
        if (c.getSalaryFrom() != null && c.getSalaryTo() != null && c.getSalaryFrom() > c.getSalaryTo()) {
            model.addAttribute("error", new BusinessException(
                    "Зарплата «від» (" + c.getSalaryFrom() + ") більша за «до» (" + c.getSalaryTo() + ") — діапазон зарплати не застосовано.",
                    "Поміняйте значення місцями або виправте одне з них і натисніть «Знайти».",
                    "Вказуйте в полі «від» меншу суму, ніж у полі «до»."));
            c.setSalaryFrom(null);
            c.setSalaryTo(null);
        }
    }

    static void report(BindingResult binding, Model model) {
        if (binding.hasFieldErrors()) {
            String fields = String.join(", ", binding.getFieldErrors().stream()
                    .map(e -> "«" + e.getRejectedValue() + "»").toList());
            model.addAttribute("error", new BusinessException(
                    "Фільтр зарплати містить нечислове значення " + fields + " — фільтр не застосовано.",
                    "Введіть суму цифрами без пробілів, наприклад 30000, і натисніть «Знайти».",
                    "Використовуйте стрілки поля або вводьте лише цифри."));
        }
    }
}
