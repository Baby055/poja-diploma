package hei.poja.io.security;

import static org.springframework.security.config.Customizer.withDefaults;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConf {
  private final AppUserDetailsService appUserDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    var provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(appUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/ping", "/health/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/students", "/teachers", "/admins")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.GET, "/courses/**", "/groups/**", "/course-assignments/**")
                    .authenticated()
                    .requestMatchers(
                        HttpMethod.POST, "/courses/**", "/groups/**", "/course-assignments/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.PUT, "/courses/**", "/groups/**", "/course-assignments/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.DELETE, "/courses/**", "/groups/**", "/course-assignments/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/exams")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/exams")
                    .hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/*/grades")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/students/*/grades")
                    .hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/students/*/group")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/*/group-history")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/students/*/transcript")
                    .authenticated()
                    .requestMatchers("/promotions/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .httpBasic(withDefaults());
    return http.build();
  }
}
