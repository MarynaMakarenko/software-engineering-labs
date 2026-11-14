package ua.knu.jobmatch.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.ModelAndView;
import ua.knu.jobmatch.config.AccountPrincipal;
import ua.knu.jobmatch.service.BusinessException;
import ua.knu.jobmatch.service.NotFoundException;

import java.net.URI;

@ControllerAdvice
public class GlobalControllerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("me")
    public AccountPrincipal me(@AuthenticationPrincipal AccountPrincipal principal) {
        return principal;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ExceptionHandler({NotFoundException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView notFound(Exception e) {
        ModelAndView mv = new ModelAndView("error/404");
        mv.addObject("detail", e instanceof NotFoundException ? e.getMessage() : "Сторінку не знайдено");
        return mv;
    }

    /** Порушення бізнес-правила: повертаємо користувача на попередню сторінку з повідомленням з 3 частин. */
    @ExceptionHandler(BusinessException.class)
    public String business(BusinessException e, HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("error", e);
        return "redirect:" + safeReferer(request);
    }

    static String safeReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            return "/";
        }
        try {
            URI uri = URI.create(referer);
            if (uri.getHost() != null && !uri.getHost().equals(request.getServerName())) {
                return "/";
            }
            String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException ex) {
            return "/";
        }
    }
}
