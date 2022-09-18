package com.project.sidefit.domain.service.auth;

import com.project.sidefit.advice.exception.CEmailLoginFailedException;
import com.project.sidefit.advice.exception.CRefreshTokenException;
import com.project.sidefit.advice.exception.CTokenNotFound;
import com.project.sidefit.advice.exception.CUserNotFoundException;
import com.project.sidefit.domain.entity.ConfirmationToken;
import com.project.sidefit.domain.entity.user.Token;
import com.project.sidefit.domain.entity.user.User;
import com.project.sidefit.domain.entity.user.UserPrev;
import com.project.sidefit.domain.repository.ConfirmationTokenRepository;
import com.project.sidefit.domain.repository.TokenRepository;
import com.project.sidefit.domain.repository.UserRepository;
import com.project.sidefit.domain.repository.UserPrevRepository;
import com.project.sidefit.domain.service.dto.TokenDto;
import com.project.sidefit.domain.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.url}")
    private String url;

    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final CustomTokenProviderService customTokenProviderService;
//    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserPrevRepository userPrevRepository;


    /**
     * 해당 email을 가진 user가 존재하는지? email 중복 체크
     *
     * @return true: 중복O, false: 중복X
     */
    public boolean validateDuplicatedEmail(String email) {

        // User 테이블, UserPrev 테이블 모두 없어야 한다.
        return userRepository.existsByEmail(email) || userPrevRepository.existsByEmail(email);
    }

    public boolean validateDuplicatedNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    @Transactional
    public Long saveUserPrev(String email, String password) {

        String encodedPassword = passwordEncoder.encode(password);

        UserPrev userPrev = UserPrev.createUserPrev(email, encodedPassword);
        UserPrev savedUserPrev = userPrevRepository.save(userPrev);

        return savedUserPrev.getId();
    }

    // 인증 메일 전송
    @Transactional
    public void sendAuthEmail(String receiveEmail) {

        // email 로 인증토큰 생성
        ConfirmationToken confirmationToken = ConfirmationToken.createEmailConfirmationToken(receiveEmail);
        confirmationTokenRepository.save(confirmationToken);

        // 인증토큰 링크 메일 전송
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(receiveEmail);
        mailMessage.setSubject("sidefit 회원가입 이메일 인증");

        // ~~~/uuid 형태
        // TODO EC2 퍼블릭 ip: http://3.39.135.44:8080/ >> url prefix 변수 생성해서 yml 파일로부터 운영환경에 따른 자동 설정되도록 변경
//        mailMessage.setText("http://localhost:8080/api/auth/confirm-email/" + confirmationToken.getToken());
        mailMessage.setText(url + confirmationToken.getToken());

        mailService.sendMail(mailMessage);
    }

    @Transactional
    public void sendAuthEmailAgain(String receiveEmail) {

        // TODO 예외 처리 >> 토큰 없으면 새로 생성하도록?
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByEmail(receiveEmail).orElseThrow(() -> new RuntimeException("해당 토큰 없음"));
        confirmationToken.updateToken();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(receiveEmail);
        mailMessage.setSubject("sidefit 회원가입 이메일 인증");

        // TODO http://3.39.135.44:8080/
//        mailMessage.setText("http://localhost:8080/api/auth/confirm-email/" + confirmationToken.getToken());
        mailMessage.setText(url + confirmationToken.getToken());
        mailService.sendMail(mailMessage);
    }

    // 토큰 검증
    @Transactional
    public void confirmEmail(String token) {
        // 넘어온 uuid 값 >> ConfirmationToken 의 pk
        // pk, 현재 시간, 만료여부 로 토큰 조회
        // 토큰 없는 경우 예외 >> 인증 메일 재전송 알림
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByTokenAndExpirationAfterAndExpired(token, LocalDateTime.now(), false).orElseThrow(CTokenNotFound::new);

        // 토큰 useToken() 처리
        confirmationToken.useToken();

        // TODO 토큰 삭제처리?

        // UserPrev 의 enable = true 로 변경
        // TODO orElseThrow() 에 예외 넣기
        UserPrev userPrev = userPrevRepository.findByEmailAndEnable(confirmationToken.getEmail(), false).orElseThrow();
        userPrev.confirmSuccess();
    }

    // 이메일 인증 여부 확인
    public boolean checkEmailAuth(String email) {

        return userPrevRepository.existsByEmailAndEnable(email, true);
    }

    @Transactional
    public void join(String email, String nickname, String job) {

        UserPrev userPrev = userPrevRepository.findByEmailAndEnable(email, true).orElseThrow(RuntimeException::new);

        // 넘어온 데이터 + email이 일치하는 userPrev 조합으로 User 생성
        User user = User.createUser(email, userPrev.getPassword(), nickname, job);

        // User 저장
        userRepository.save(user);

        // UserPrev 삭제
        userPrevRepository.delete(userPrev);
    }

    @Transactional
    public TokenDto login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(CEmailLoginFailedException::new);

        // 회원 패스워드 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CEmailLoginFailedException();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        TokenDto tokenDto = customTokenProviderService.createTokenDto(authentication);

//        TokenDto tokenDto = jwtProvider.createTokenDto(user.getId(), user.getRoles());

        // TODO RefreshToken 은 Redis 에 보관?
        // RefreshToken 레포지토리에 저장
        Token token = Token.createToken(user.getId(), tokenDto.getRefreshToken());
        tokenRepository.save(token);

        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(String accessToken, String refreshToken) {

//        if (!jwtProvider.validationToken(refreshToken)) {
//            throw new CRefreshTokenException();
//        }

        if (!customTokenProviderService.validate(refreshToken)) {
            throw new CRefreshTokenException();
        }

        Long id = customTokenProviderService.getUserIdFromToken(accessToken);

//        String userPk = jwtProvider.getAuthentication(accessToken).getName();
//        User user = userRepository.findById(Long.parseLong(userPk)).orElseThrow(CUserNotFoundException::new);

        User user = userRepository.findById(id).orElseThrow(CUserNotFoundException::new);

        Token token = tokenRepository.findByKey(user.getId()).orElseThrow(CRefreshTokenException::new);

        if (!token.getRefreshToken().equals(refreshToken)) {
            throw new CRefreshTokenException();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );

        TokenDto tokenDto = customTokenProviderService.createTokenDto(authentication);

//        TokenDto tokenDto = jwtProvider.createTokenDto(user.getId(), user.getRoles());
        token.updateToken(tokenDto.getRefreshToken());

        return tokenDto;
    }
}
