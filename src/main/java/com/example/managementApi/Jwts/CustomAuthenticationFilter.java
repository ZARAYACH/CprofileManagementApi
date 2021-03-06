package com.example.managementApi.Jwts;

import com.auth0.jwt.algorithms.Algorithm;
import com.example.managementApi.User.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.*;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private JwtsService jwtsService = new JwtsService();

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtsService jwtsService) {
        this.authenticationManager = authenticationManager;
        this.jwtsService = jwtsService;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            EmailPasswordModal emailPasswordModal = new ObjectMapper().readValue(request.getInputStream(), EmailPasswordModal.class);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(emailPasswordModal.getEmail(),
                    emailPasswordModal.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            return authentication;

        } catch (IOException e) {
            response.setStatus(BAD_REQUEST.value());
            response.setContentType(APPLICATION_JSON_VALUE);
            HashMap<String,String> error = new HashMap<>();
            error.put("error",e.getMessage());
            System.out.println(e.getMessage());
            try {
                new ObjectMapper().writeValue(response.getOutputStream(),error.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        User user = (User) authentication.getPrincipal();
        Algorithm algorithmAccess = Algorithm.HMAC256("secretsecretsecretsecretsecretsecretsecret".getBytes(StandardCharsets.UTF_8));
        Algorithm algorithmRefresh = Algorithm.HMAC256("refreshrefreshrefreshrefreshrefreshrefreshrefresh".getBytes(StandardCharsets.UTF_8));
        String access_token = jwtsService.createJwtAccessToken(request, user);
        String refresh_token = jwtsService.createJwtRefreshToken(request, user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        HashMap<String, String> error = new HashMap<>();
        error.put("error", failed.toString());
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }


}