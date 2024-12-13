package vn.edu.iuh.fit.week05.backend.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.Company;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.repositories.CompanyRepository;


import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final DataSource dataSource;
    private final CandidateRepository candidateRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public SecurityConfig(DataSource dataSource, CandidateRepository candidateRepository, CompanyRepository companyRepository) {
        this.dataSource = dataSource;
        this.candidateRepository = candidateRepository;
        this.companyRepository = companyRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            // Lấy email của người dùng
            String email = authentication.getName();
            // Tùy vào vai trò, lấy thông tin từ database
            if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_CANDIDATE"))) {
                Candidate candidate = candidateRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Candidate not found"));
                request.getSession().setAttribute("user", candidate);
                response.sendRedirect("/candidate/home");
            } else if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_COMPANY"))) {
                Company company = companyRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Company not found"));
                request.getSession().setAttribute("user", company);
                response.sendRedirect("/company/home");
            } else {
                response.sendRedirect("/access-denied");
            }
        };
    }


    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            request.getSession().setAttribute("error", "Invalid email or password. Please try again!");
            response.sendRedirect("/signin");
        };
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager() {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);

        userDetailsManager.setUsersByUsernameQuery(
                "SELECT email AS username, password, true AS enabled " +
                        "FROM (" +
                        "   SELECT email, password FROM candidate " +
                        "   UNION ALL " +
                        "   SELECT email, password FROM company" +
                        ") AS users WHERE email = ?");

        userDetailsManager.setAuthoritiesByUsernameQuery(
                "SELECT email AS username, role AS authority " +
                        "FROM (" +
                        "   SELECT email, 'ROLE_CANDIDATE' AS role FROM candidate " +
                        "   UNION ALL " +
                        "   SELECT email, 'ROLE_COMPANY' AS role FROM company" +
                        ") AS roles WHERE email = ?");

        return userDetailsManager;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(jdbcUserDetailsManager());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF for testing purposes
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/").permitAll()
                        .requestMatchers("/candidate/**").hasRole("CANDIDATE") // ROLE_CANDIDATE
                        .requestMatchers("/company/**").hasRole("COMPANY") // ROLE_COMPANY
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/signin") // Trang đăng nhập
                        .successHandler(successHandler())
                        .failureHandler(failureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // Đường dẫn logout
                        .logoutSuccessUrl("/signin") // Trang chuyển hướng sau khi logout
                        .invalidateHttpSession(true) // Hủy phiên đăng nhập
                        .deleteCookies("JSESSIONID") // Xóa cookie phiên làm việc
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied") // Chỉ định trang tùy chỉnh khi bị lỗi 403
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}




