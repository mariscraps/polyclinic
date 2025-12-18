package fa.ru.polyclinic.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            httpStatus = HttpStatus.valueOf(statusCode);
        }

        String message;
        switch (httpStatus) {
            case NOT_FOUND:
                message = "Страница не найдена. Возможно, она была удалена или перемещена.";
                break;
            case FORBIDDEN:
                message = "Доступ запрещён. У вас нет прав для просмотра этой страницы.";
                break;
            case INTERNAL_SERVER_ERROR:
                message = "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.";
                break;
            default:
                message = "Произошла ошибка при обработке вашего запроса.";
        }

        model.addAttribute("errorMessage", message);
        model.addAttribute("statusCode", httpStatus.value());
        model.addAttribute("errorReason", httpStatus.getReasonPhrase());
        return "error";
    }
}