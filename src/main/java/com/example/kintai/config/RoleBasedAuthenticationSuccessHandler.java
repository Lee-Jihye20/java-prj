package com.example.kintai.config;

import com.example.kintai.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication == null || !authentication.isAuthenticated()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/employee/dashboard");
            return;
        }

        User user = (User) principal;
        if ("ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/employee/dashboard");
        }
    }
}
