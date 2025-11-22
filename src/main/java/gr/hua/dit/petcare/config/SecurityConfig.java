package gr.hua.dit.petcare.config;

import gr.hua.dit.petcare.security.ApplicationUserDetailsService;
import gr.hua.dit.petcare.security.JwtAuthFilter;
import gr.hua.dit.petcare.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // για @PreAuthorize κλπ, αν το θες αργότερα σε services/controllers
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final ApplicationUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                          ApplicationUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Κύριο SecurityFilterChain.
     *
     * Εδώ ρυθμίζουμε:
     * - JWT stateless security
     * - ποια endpoints είναι ανοιχτά (auth, swagger κλπ)
     * - εξαίρεση για H2 console (αν τη χρησιμοποιείς)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider) throws Exception {

        http
                // Δεν χρειαζόμαστε CSRF για καθαρό REST + JWT
                .csrf(csrf -> csrf.disable())

                // CORS ρυθμίσεις (βλέπε corsConfigurationSource)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Δεν κρατάμε sessions, όλα με JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Custom entry point για 401/json αντί για redirect σε login page
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )

                // Ποια endpoints είναι ανοιχτά
                .authorizeHttpRequests(auth -> auth
                        // AUTH REST endpoints (login/register)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger / OpenAPI για development
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // H2 console (αν τη χρησιμοποιείς)
                        .requestMatchers("/h2-console/**").permitAll()

                        // (Προαιρετικά) static resources / root paths
                        .requestMatchers(
                                "/",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Οτιδήποτε άλλο → authenticated
                        .anyRequest().authenticated()
                )

                // Ορίζουμε ποιο AuthenticationProvider θα χρησιμοποιείται
                .authenticationProvider(authenticationProvider)

                // Προσθέτουμε το JWT filter ΠΡΙΝ το UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Για να παίζει η H2 console σε iframe
        http.headers(headers ->
                headers.frameOptions(frameOptions -> frameOptions.disable())
        );

        return http.build();
    }

    /**
     * DaoAuthenticationProvider που χρησιμοποιεί:
     * - το δικό μας UserDetailsService
     * - το global PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder,
                                                         UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Εκθέτουμε το AuthenticationManager ώστε να μπορεί
     * να γίνει inject στο AuthRestController ή στο UserServiceImpl
     * για login (username/password auth).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS config – αυτή τη στιγμή είναι "χαλαρή" για development.
     * Μπορείς αργότερα να την σφίξεις (συγκεκριμένα origins).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Για development: επιτρέπει όλα τα origins.
        // Αργότερα μπορείς να βάλεις π.χ. List.of("http://localhost:3000")
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Optional, αλλά καλό να υπάρχει αν θες να inject-άρεις
     * το UserDetailsService από Spring Security API.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }
}
