package com.tui.proof.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tui.proof.ws.controller.AuthenticationController;
import com.tui.proof.ws.controller.OrderController;

/**
 * Pilotes security configuration
 * @author maura.piredda
 */
@Configuration
@EnableWebSecurity
public class PilotesSecurityConfiguration extends WebSecurityConfigurerAdapter
{
    /** The authority needed to search the orders by customers */
    public static final String SEARCH_AUTHORITY = "SEARCH";

    @Autowired
    private JwtConfiguration jwtConfiguration;

    /**
     * The only API secured is {@link OrderController#searchByCustomer(com.tui.proof.model.Customer)}
     * @see JwtAuthorizationFilter
     * @see AuthenticationController
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        JwtAuthorizationFilter filter = new JwtAuthorizationFilter(jwtConfiguration.getSecretKey());
        http.csrf().disable()
                .addFilterAfter(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/**/orders/searchByCustomer/**").hasAuthority(SEARCH_AUTHORITY)
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}