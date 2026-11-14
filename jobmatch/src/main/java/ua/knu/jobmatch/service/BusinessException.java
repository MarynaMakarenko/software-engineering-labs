package ua.knu.jobmatch.service;

/**
 * Порушення бізнес-правила. Повідомлення для актора має три частини:
 * сутність помилки, як виправити зараз, як запобігти надалі.
 */
public class BusinessException extends RuntimeException {
    private final String fixNow;
    private final String prevent;

    public BusinessException(String what, String fixNow, String prevent) {
        super(what);
        this.fixNow = fixNow;
        this.prevent = prevent;
    }

    public String getWhat() { return getMessage(); }
    public String getFixNow() { return fixNow; }
    public String getPrevent() { return prevent; }
}
