package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/product-ingredients").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/product-ingredients").hasRole("admin")
                        .requestMatchers(HttpMethod.PUT, "/api/product-ingredients").hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/product-ingredients").hasRole("admin")

                        .requestMatchers(HttpMethod.POST, "/api/reservations").hasRole("admin") // createReservation
                        .requestMatchers(HttpMethod.GET, "/api/reservations").hasAnyRole("admin", "waiter", "cashier") // getAllReservations
                        .requestMatchers(HttpMethod.GET, "/api/reservations/{id}").hasAnyRole("admin", "waiter", "cashier") // getReservationById
                        .requestMatchers(HttpMethod.GET, "/api/reservations/table/{tableId}").hasAnyRole("admin", "waiter", "cashier") // getReservationsByTable
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/{id}/cancel").hasRole("admin") // cancelReservation
                         .requestMatchers(HttpMethod.PUT, "/api/reservations/{id}/complete").hasRole("admin") // completeReservation
                         .requestMatchers(HttpMethod.GET, "/api/reservations/status/{status}").hasAnyRole("admin", "waiter", "cashier") // getReservationsByStatus
                         .requestMatchers(HttpMethod.GET, "/api/reservations/today").hasAnyRole("admin", "waiter", "cashier") // getTodayReservations
                         .requestMatchers(HttpMethod.PUT, "/api/reservations/{id}").hasRole("admin") // updateReservation
                         .requestMatchers(HttpMethod.GET, "/api/reservations/date-range").hasAnyRole("admin", "waiter", "cashier") // getReservationsByDateRange

                        .requestMatchers(HttpMethod.POST,"/api/stocks/**").hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/api/stocks/**").hasAnyRole("admin", "waiter", "cashier")

                        .requestMatchers(HttpMethod.GET,"/api/dining-tables/").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/dining-tables/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/dining-tables/available").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/dining-tables").hasRole("admin")
                        .requestMatchers(HttpMethod.PUT,"/api/dining-tables/{id}").hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE,"/api/dining-tables/{id}").hasRole("admin")
                        .requestMatchers(HttpMethod.PATCH,"/api/dining-tables/{id}/status/{status}").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/dining-tables/{id}/capacity/{capacity}").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/api/dining-tables/{id}/table-number/{tableNumber}").authenticated()
                        .requestMatchers("/api/dining-tables/filter").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("admin") // createProduct
                        .requestMatchers(HttpMethod.GET, "/api/products/{id}").hasAnyRole("admin", "waiter", "cashier") // getProductById
                        .requestMatchers(HttpMethod.GET, "/api/products").hasAnyRole("admin", "waiter", "cashier") // getAllProducts
                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}").hasRole("admin") // updateProduct
                        .requestMatchers(HttpMethod.DELETE, "/api/products/{id}").hasRole("admin") // deleteProduct

                        .requestMatchers(HttpMethod.POST, "/api/stock-movements").hasRole("admin") // createStockMovement
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/{id}").hasAnyRole("admin", "waiter") // getById
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements").hasAnyRole("admin", "waiter") // getAll
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/product/{productId}").hasAnyRole("admin", "waiter") // getByProductId
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/reason/{reason}").hasAnyRole("admin", "waiter") // getByReason
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/date-range").hasAnyRole("admin", "waiter") // getByDateRange
                        .requestMatchers(HttpMethod.GET, "/api/stock-movements/total-change/{productId}").hasAnyRole("admin", "waiter") // getTotalStockChange
                        .requestMatchers(HttpMethod.PUT, "/api/stock-movements/{id}").hasRole("admin") // updateStockMovement
                        .requestMatchers(HttpMethod.DELETE, "/api/stock-movements/{id}").hasRole("admin") // deleteStockMovement

                        .requestMatchers(HttpMethod.GET, "/api/payments").hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/api/payments/**").hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/payments/{id}").hasRole("admin")
                        .requestMatchers(HttpMethod.POST, "/api/payments").hasAnyRole("admin", "cashier")

                        .requestMatchers("/api/dashboard/**").authenticated()
                        .requestMatchers("/api/activity-logs/**").authenticated()

                        .requestMatchers("/api/users/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/orders").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/order-items/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/roles/**").authenticated()
                        .requestMatchers("/api/salons/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/settings").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/settings").hasRole("admin")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}