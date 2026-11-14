package ua.knu.jobmatch.web.form;

final class Forms {
    private Forms() { }

    static String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
