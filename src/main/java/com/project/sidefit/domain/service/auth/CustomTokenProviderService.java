package com.project.sidefit.domain.service.auth;

import com.project.sidefit.config.security.OAuth2Config;
import com.project.sidefit.config.security.token.UserPrincipal;
import com.project.sidefit.domain.service.dto.TokenDto;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomTokenProviderService {

//    @Value("${app.auth.tokenSecret}")
//    private String secretKey;
//
//    @Value("${app.auth.accessTokenExpirationMsec: 60 * 60 * 1000L}")
//    private final Long ACCESSTOKEN_VALID_MILLISECOND;
//
//    @Value("${app.auth.refreshTokenExpirationMsec: 14 * 24 * 60 * 60 * 1000L}")
//    private final Long REFRESHTOKEN_VALID_MILLISECOND;

    private final OAuth2Config oAuth2Config;
    private final CustomUserDetailsService userDetailsService;

    private final String ROLES = "roles";

//    private String secretKey = oAuth2Config.getAuth().getTokenSecret();
//
//
//    @PostConstruct
//    protected void init() {
//        secretKey = Base64UrlCodec.BASE64URL.encode(secretKey.getBytes(StandardCharsets.UTF_8));
//    }


//    public TokenDto createTokenDto(Long userPk, RoleType role) {
//        Claims claims = Jwts.claims().setSubject(String.valueOf(userPk));
//        claims.put(ROLES, role);
//
//        Date now = new Date();
//        Date accessTokenExpiresIn = new Date(now.getTime() + oAuth2Config.getAuth().getAccessTokenExpirationMsec());
//        Date refreshTokenExpiresIn = new Date(now.getTime() + oAuth2Config.getAuth().getRefreshTokenExpirationMsec());
//
//        String accessToken = Jwts.builder()
//                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(accessTokenExpiresIn)
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();;
//
//        String refreshToken = Jwts.builder()
//                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
//                .setExpiration(refreshTokenExpiresIn)
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
//
//        return TokenDto.builder()
////                .grantType("Bearer")
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
////                .accessTokenExpireDate(oAuth2Config.getAuth().)
//                .build();
//    }

//    private String getRefreshToken(Date refreshTokenExpiresIn) {
//        return Jwts.builder()
//                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
//                .setExpiration(refreshTokenExpiresIn)
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
//    }
//
//    private String getAccessToken(Claims claims, Date now, Date accessTokenExpiresIn) {
//        return Jwts.builder()
//                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(accessTokenExpiresIn)
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
//    }








    public TokenDto createTokenDto(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date accessTokenExpiresIn = new Date(now.getTime() + oAuth2Config.getAuth().getAccessTokenExpirationMsec());
        Date refreshTokenExpiresIn = new Date(now.getTime() + oAuth2Config.getAuth().getRefreshTokenExpirationMsec());

        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(now)
                .setExpiration(accessTokenExpiresIn)
                .signWith(SignatureAlgorithm.HS256, oAuth2Config.getAuth().getTokenSecret())
                .compact();;

        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setExpiration(refreshTokenExpiresIn)
                .signWith(SignatureAlgorithm.HS256, oAuth2Config.getAuth().getTokenSecret())
                .compact();

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);

        return Long.parseLong(claims.getSubject());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(oAuth2Config.getAuth().getTokenSecret()).parseClaimsJws(token).getBody();
    }

    public Authentication getAuthentication(String token) {
        Long id = getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserById(id);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return authentication;
    }

    // access token의 남은 유효시간
    public Long getExpiration(String token) {
        // accessToken 남은 유효시간
        Date expiration = parseClaims(token).getExpiration();

        // 현재 시간
        Long now = new Date().getTime();

        //시간 계산
        return (expiration.getTime() - now);
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(oAuth2Config.getAuth().getTokenSecret()).parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 Jwt 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 Jwt 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 Jwt 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 Jwt 토큰입니다.");
        }
        return false;
    }
}
