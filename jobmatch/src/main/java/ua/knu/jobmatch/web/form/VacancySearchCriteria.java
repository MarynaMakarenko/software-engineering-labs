package ua.knu.jobmatch.web.form;

/** Критерії пошуку вакансій (G1). Усі поля необов'язкові. */
public class VacancySearchCriteria {
    private String position;
    private String company;
    private String city;
    private Integer salaryFrom;

    public String getPosition() { return position; }
    public void setPosition(String v) { position = Forms.trim(v); }
    public String getCompany() { return company; }
    public void setCompany(String v) { company = Forms.trim(v); }
    public String getCity() { return city; }
    public void setCity(String v) { city = Forms.trim(v); }
    public Integer getSalaryFrom() { return salaryFrom; }
    public void setSalaryFrom(Integer v) { salaryFrom = v; }
}
