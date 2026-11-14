package ua.knu.jobmatch.web.form;

import ua.knu.jobmatch.domain.EducationLevel;
import ua.knu.jobmatch.domain.JobStatus;

/** Критерії пошуку резюме (R1). Усі поля необов'язкові. */
public class PersonSearchCriteria {
    private String name;
    private EducationLevel minEducation;
    private String company;
    private String position;
    private Integer salaryFrom;
    private Integer salaryTo;
    private JobStatus status;

    public boolean isEmpty() {
        return name == null && minEducation == null && company == null && position == null
                && salaryFrom == null && salaryTo == null && status == null;
    }

    public String getName() { return name; }
    public void setName(String v) { name = Forms.trim(v); }
    public EducationLevel getMinEducation() { return minEducation; }
    public void setMinEducation(EducationLevel v) { minEducation = v; }
    public String getCompany() { return company; }
    public void setCompany(String v) { company = Forms.trim(v); }
    public String getPosition() { return position; }
    public void setPosition(String v) { position = Forms.trim(v); }
    public Integer getSalaryFrom() { return salaryFrom; }
    public void setSalaryFrom(Integer v) { salaryFrom = v; }
    public Integer getSalaryTo() { return salaryTo; }
    public void setSalaryTo(Integer v) { salaryTo = v; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus v) { status = v; }
}
