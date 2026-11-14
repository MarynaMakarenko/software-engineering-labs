package ua.knu.jobmatch.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ua.knu.jobmatch.repository.ApplicationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Інтеграційні тести R2 «Підбір кандидатів на вакансію»: Controller → Service → Repository → H2 (Flyway-дані).
 * Перевіряють узгодженість інтерфейсів класів, відтестованих модульно.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CandidateMatchingIntegrationTest {
    private static final String RECRUITER = "olena.koval@profikadr.ua";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ApplicationRepository applications;

    @Test
    @WithUserDetails(RECRUITER)
    void mainFlowShowsRankedCandidates() throws Exception {
        mvc.perform(get("/recruiter/vacancies/1/matches"))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/candidate-matches"))
                .andExpect(model().attribute("minScore", 50))
                .andExpect(model().attribute("results", hasSize(4)))
                .andExpect(content().string(containsString("Петренко Іван Олегович")))
                .andExpect(content().string(not(containsString("Олійник"))));   // не шукає роботу
    }

    @Test
    @WithUserDetails(RECRUITER)
    void proposeCreatesApplication() throws Exception {
        long before = applications.count();
        mvc.perform(post("/recruiter/vacancies/1/propose").with(csrf())
                        .param("personId", "3").param("comment", "Сильний бекенд").param("minScore", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/vacancies/1/matches?minScore=50"))
                .andExpect(flash().attribute("success", containsString("Петренко Іван Олегович")));
        assertThat(applications.count()).isEqualTo(before + 1);
        assertThat(applications.findByVacancyIdAndPersonId(1L, 3L)).isPresent();
    }

    @Test
    @WithUserDetails(RECRUITER)
    void partialMatchesShowMore() throws Exception {   // A1
        mvc.perform(get("/recruiter/vacancies/1/matches").param("minScore", "25"))
                .andExpect(model().attribute("results", hasSize(greaterThan(4))));
    }

    @Test
    @WithUserDetails(RECRUITER)
    void emptyStateOffersPartial() throws Exception {   // A1
        mvc.perform(get("/recruiter/vacancies/12/matches"))
                .andExpect(model().attribute("results", hasSize(0)))
                .andExpect(content().string(containsString("Показати часткові збіги")));
    }

    @Test
    @WithUserDetails(RECRUITER)
    void invalidThresholdFallsBackWithMessage() throws Exception {   // A2
        mvc.perform(get("/recruiter/vacancies/1/matches").param("minScore", "150"))
                .andExpect(model().attribute("minScore", 50))
                .andExpect(content().string(containsString("від 0 до 100")));
    }

    @Test
    @WithUserDetails(RECRUITER)
    void duplicateProposalRejected() throws Exception {   // A3
        long before = applications.count();
        mvc.perform(post("/recruiter/vacancies/1/propose").with(csrf()).param("personId", "4")
                        .header("Referer", "http://localhost/recruiter/vacancies/1/matches"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", hasProperty("what", containsString("вже існує"))));
        assertThat(applications.count()).isEqualTo(before);
    }

    @Test
    @WithUserDetails(RECRUITER)
    void closedVacancyNotMatched() throws Exception {   // A4
        mvc.perform(get("/recruiter/vacancies/4/matches"))
                .andExpect(model().attribute("results", hasSize(0)))
                .andExpect(content().string(containsString("Вакансію закрито")));
    }

    @Test
    @WithUserDetails(RECRUITER)
    void longCommentKeepsFormAndText() throws Exception {   // A5
        String c501 = "к".repeat(501);
        mvc.perform(post("/recruiter/vacancies/1/propose").with(csrf()).param("personId", "3").param("comment", c501))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/propose"))
                .andExpect(model().attributeHasFieldErrors("form", "comment"))
                .andExpect(content().string(containsString(c501)));
        assertThat(applications.findByVacancyIdAndPersonId(1L, 3L)).isEmpty();
    }

    @Test
    @WithUserDetails(RECRUITER)
    void deletedVacancyGives404() throws Exception {   // A7
        mvc.perform(get("/recruiter/vacancies/999/matches")).andExpect(status().isNotFound())
                .andExpect(content().string(containsString("404")));
    }

    @Test
    @WithUserDetails("ivan.petrenko@example.com")
    void candidateCannotOpenRecruiterPage() throws Exception {
        mvc.perform(get("/recruiter/vacancies/1/matches")).andExpect(status().isForbidden());
    }

    @Test
    void anonymousRedirectedToLogin() throws Exception {
        mvc.perform(get("/recruiter/vacancies/1/matches"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
