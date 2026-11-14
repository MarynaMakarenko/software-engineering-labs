package ua.knu.jobmatch.web;

import org.springframework.ui.Model;
import ua.knu.jobmatch.matching.MatchingService;
import ua.knu.jobmatch.service.BusinessException;

/** Розбір поля «Мін. оцінка, %» (R2 A2): ціле 0..100, інакше — поріг за замовчуванням і повідомлення. */
final class MatchThreshold {
    private MatchThreshold() { }

    static int parse(String raw, Model model) {
        return parse(raw, null, model);
    }

    /** prev — поріг, з яким було побудовано попередній список (SRS: «поріг лишається попереднім»). */
    static int parse(String raw, String prev, Model model) {
        int fallback = MatchingService.DEFAULT_THRESHOLD;
        try {
            int p = Integer.parseInt(prev == null ? "" : prev.trim());
            if (p >= 0 && p <= 100) {
                fallback = p;
            }
        } catch (NumberFormatException ignored) {
            // попереднього порогу немає — використовуємо поріг за замовчуванням
        }
        int value;
        try {
            value = Integer.parseInt(raw == null ? "" : raw.trim());
        } catch (NumberFormatException e) {
            value = -1;
        }
        if (value < 0 || value > 100) {
            model.addAttribute("error", new BusinessException(
                    "Мінімальна оцінка «" + raw + "» має бути цілим числом від 0 до 100.",
                    "Введіть ціле число від 0 до 100, наприклад 50, і натисніть «Оновити».",
                    "Використовуйте кнопку «Показати часткові збіги» для порогу 25 %."));
            model.addAttribute("minScore", fallback);
            return fallback;
        }
        model.addAttribute("minScore", value);
        return value;
    }
}
