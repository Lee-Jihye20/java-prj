package com.example.kintai.config;

import com.example.kintai.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomWebAuthenticationDetailsSource authenticationDetailsSource;

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler() {
        return new RoleBasedAuthenticationSuccessHandler();
    }

        @Bean

        public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationProvider customAuthenticationProvider) throws Exception {

            http

                    .authenticationProvider(customAuthenticationProvider)

                    .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/h2-console/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationDetailsSource(authenticationDetailsSource)
                        .loginProcessingUrl("/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (request.getRequestURI().startsWith(request.getContextPath() + "/admin")) {
                                response.sendRedirect(request.getContextPath() + "/employee/dashboard?accessDenied=1");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login?accessDenied=1");
                            }
                        })
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 本番環境ではBCryptPasswordEncoderを使用すべき
        // return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }
}
