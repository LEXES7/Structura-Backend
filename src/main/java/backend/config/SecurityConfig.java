package backend.config;

import backend.filter.JwtFilter;
import backend.model.User;
import backend.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.Date;
import java.util.logging.Logger;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class.getName());
    private final UserService userService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(UserService userService, JwtFilter jwtFilter) {
        this.userService = userService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Use the updated cors(Customizer)
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow OPTIONS for CORS
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/posts").permitAll()
                        .requestMatchers("/api/events/upcoming").permitAll()
                        .requestMatchers("/api/events").permitAll() // For GET
                        .requestMatchers("/api/events/**").authenticated() // POST, PUT, DELETE need auth
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/oauth2/authorization/**").permitAll()
                        .requestMatchers("/login/oauth2/code/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")
                        .successHandler((request, response, authentication) -> {
                            if (authentication instanceof OAuth2AuthenticationToken token) {
                                String email = token.getPrincipal().getAttribute("email");
                                String name = token.getPrincipal().getAttribute("name");
                                String photoUrl = token.getPrincipal().getAttribute("picture");

                                LOGGER.info("OAuth2 Login Success - Email: " + email);

                                User user = userService.findByEmail(email).orElseGet(() -> {
                                    User newUser = new User();
                                    newUser.setUsername(name.toLowerCase().replace(" ", ""));
                                    newUser.setEmail(email);
                                    newUser.setProfilePicture(photoUrl);
                                    newUser.setCreatedAt(new Date());
                                    newUser.setUpdatedAt(new Date());
                                    return userService.signupGoogle(newUser);
                                });

                                String jwt = userService.generateJwt(user.getId());
                                LOGGER.info("Generated JWT: " + jwt);
                                String redirectUrl = String.format(
                                        "http://localhost:5173/oauth-callback?token=%s&id=%s&username=%s&email=%s&isAdmin=%s&profilePicture=%s",
                                        jwt, user.getId(), user.getUsername(), user.getEmail(), user.isAdmin(), user.getProfilePicture()
                                );
                                LOGGER.info("Redirecting to: " + redirectUrl);
                                response.sendRedirect(redirectUrl);
                            } else {
                                LOGGER.warning("OAuth2 authentication failed");
                                response.sendRedirect("http://localhost:5173/signin?error=login_failed");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            LOGGER.severe("OAuth2 failure: " + exception.getMessage());
                            response.sendRedirect("http://localhost:5173/signin?error=" + exception.getMessage());
                        })
                        .failureUrl("http://localhost:5173/signin?error=authentication_failed")
                );

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // Allow frontend origin
        config.addAllowedHeader("*"); // Allow all headers
        config.addAllowedMethod("*"); // Allow all HTTP methods
        config.setMaxAge(3600L); // Cache preflight response for 1 hour
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}