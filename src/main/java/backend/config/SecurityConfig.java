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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class.getName());
    private final UserService userService;
    private final JwtFilter jwtFilter;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService, JwtFilter jwtFilter, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtFilter = jwtFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        // Add these new rules for likes and shares
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/share").permitAll()
                        .requestMatchers("/api/learns").permitAll()
                        .requestMatchers("/api/courses").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/uploads/**").permitAll()
                        .requestMatchers("/api/debug/uploads-info").permitAll()
                        .requestMatchers("/api/files/list").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews").permitAll()
                        .requestMatchers("/api/events/upcoming").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/post/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/comments/post/**").permitAll()
                        .requestMatchers("/api/comments/**").authenticated()
                        .requestMatchers("/api/events/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/posts/user").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")
                        .successHandler((request, response, authentication) -> {
                            try {
                                if (!(authentication instanceof OAuth2AuthenticationToken token)) {
                                    LOGGER.warning("OAuth2 authentication failed - not an OAuth2AuthenticationToken");
                                    response.sendRedirect("http://localhost:5173/signin?error=invalid_token");
                                    return;
                                }

                                String email = token.getPrincipal().getAttribute("email");
                                String name = token.getPrincipal().getAttribute("name");
                                String photoUrl = token.getPrincipal().getAttribute("picture");

                                if (email == null) {
                                    LOGGER.warning("Email is null from OAuth2 provider");
                                    response.sendRedirect("http://localhost:5173/signin?error=email_missing");
                                    return;
                                }

                                LOGGER.info("OAuth2 Login Success - Email: " + email);

                                // First try to find existing user
                                Optional<User> existingUserOpt = userService.findByEmail(email);
                                User user = null;

                                if (existingUserOpt.isPresent()) {
                                    user = existingUserOpt.get();
                                    LOGGER.info("Found existing user: " + user.getUsername());
                                } else {
                                    LOGGER.info("Creating new user for: " + email);
                                    try {
                                        // Create new user
                                        User newUser = new User();
                                        newUser.setUsername(name != null
                                                ? name.toLowerCase().replace(" ", "")
                                                : email.split("@")[0] + UUID.randomUUID().toString().substring(0, 4));
                                        newUser.setEmail(email);

                                        if (photoUrl != null) {
                                            newUser.setProfilePicture(photoUrl);
                                        }

                                        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                                        newUser.setCreatedAt(new Date());
                                        newUser.setUpdatedAt(new Date());

                                        // Save directly with repository to avoid null returns
                                        user = userService.signupGoogle(newUser);

                                        if (user == null) {
                                            throw new RuntimeException("User creation failed");
                                        }
                                    } catch (Exception e) {
                                        LOGGER.log(Level.SEVERE, "Error creating new user", e);
                                        response.sendRedirect("http://localhost:5173/signin?error=user_creation_failed");
                                        return;
                                    }
                                }

                                // Safety check to ensure we have a valid user
                                if (user == null || user.getId() == null) {
                                    LOGGER.severe("User or user ID is null after retrieval/creation");
                                    response.sendRedirect("http://localhost:5173/signin?error=invalid_user");
                                    return;
                                }

                                // Generate JWT with user ID
                                String jwt = userService.generateJwt(user.getId());
                                LOGGER.info("Generated JWT for userId: " + user.getId());

                                String redirectUrl = String.format(
                                        "http://localhost:5173/oauth-callback?token=%s&id=%s&username=%s&email=%s&isAdmin=%s&profilePicture=%s",
                                        jwt, user.getId(), user.getUsername(), user.getEmail(),
                                        user.isAdmin(), user.getProfilePicture()
                                );

                                LOGGER.info("Redirecting to: " + redirectUrl);
                                response.sendRedirect(redirectUrl);

                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Error in OAuth2 success handler", e);
                                try {
                                    response.sendRedirect("http://localhost:5173/signin?error=server_error");
                                } catch (Exception ignored) {
                                    LOGGER.severe("Failed to redirect after error: " + ignored.getMessage());
                                }
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            LOGGER.severe("OAuth2 failure: " + exception.getMessage());
                            response.sendRedirect("http://localhost:5173/signin?error=" + exception.getMessage());
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        LOGGER.info("Configuring CORS for http://localhost:5173");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}