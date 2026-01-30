package com.example.kintai.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String companyCode;

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.companyCode = request.getParameter("companyCode");
    }

    public String getCompanyCode() {
        return companyCode;
    }
}
