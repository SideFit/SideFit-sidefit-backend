package com.project.sidefit.config.security.token;

import com.project.sidefit.domain.service.auth.CustomTokenProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.project.sidefit.config.security.token.AuthConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOncePerRequestFilter extends OncePerRequestFilter {

    private final CustomTokenProviderService customTokenProviderService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token) && customTokenProviderService.validate(token)) {
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) customTokenProviderService.getAuthentication(token);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String token = request.getHeader(AUTH_HEADER.getValue());
        if (StringUtils.hasText(token) && token.startsWith(TOKEN_TYPE.getValue())) {
            log.info("token : {}", token.substring(7));
            return token.substring(7);
        }

        return null;
    }
}
