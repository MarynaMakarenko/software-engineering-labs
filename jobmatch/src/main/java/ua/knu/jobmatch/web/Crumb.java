package ua.knu.jobmatch.web;

import java.util.ArrayList;
import java.util.List;

/** Елемент «хлібних крихт». url == null — поточна сторінка. */
public record Crumb(String label, String url) {
    /** Пари «назва, адреса»; остання назва — поточна сторінка без адреси. */
    public static List<Crumb> of(String... items) {
        List<Crumb> list = new ArrayList<>();
        list.add(new Crumb("Головна", "/"));
        for (int i = 0; i < items.length; i += 2) {
            list.add(new Crumb(items[i], i + 1 < items.length ? items[i + 1] : null));
        }
        return list;
    }
}
