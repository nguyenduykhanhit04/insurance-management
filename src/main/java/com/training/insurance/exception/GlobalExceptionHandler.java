package com.training.insurance.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNotFound(NoResourceFoundException e) throws NoResourceFoundException {
        throw e;
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
        return "error";
    }
}
