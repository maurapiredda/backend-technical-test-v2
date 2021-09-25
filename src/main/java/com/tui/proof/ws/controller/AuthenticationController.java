package com.tui.proof.ws.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tui.proof.PilotesApplication;
import com.tui.proof.security.JwtConfiguration;
import com.tui.proof.security.PilotesSecurityConfiguration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = PilotesApplication.VERSION_1_0 + "/auth")
@Tag(name = "Authentication")
public class AuthenticationController
{
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtConfiguration jwtConfiguration;

    @Operation(tags = "Authentication", summary = "Authenticates an user and returns a valid JWT.")
    @ApiResponse(responseCode = "200", description = "Authentication successfull.")

    @PostMapping("/login")
    public String login(
            @RequestParam
            String username,
            @RequestParam
            String password)
    {
        User user = authenticate(username, password);

        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Long now = System.currentTimeMillis();
        Long expiryTime = now + jwtConfiguration.getJwtExpiration() * 1000;

        String token = Jwts
                .builder()
                .setId("pilotes-jwt")
                .setSubject(username)
                .claim("authorities", authorities)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryTime))
                .signWith(SignatureAlgorithm.HS512, jwtConfiguration.getSecretKey())
                .compact();

        return token;
    }

    // Let suppose that the basic authentication is successful and that the "admin" user has the SEARCH authority, while
    // any other user hasn't
    private User authenticate(String username, String password)
    {
        List<String> authorities = new ArrayList<String>();
        if (username.equals("admin"))
        {
            authorities.add(PilotesSecurityConfiguration.SEARCH_AUTHORITY);
        }

        String[] array = authorities.toArray(new String[] {});
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList(array);
        return new User(username, passwordEncoder.encode(password), grantedAuthorities);
    }
}
