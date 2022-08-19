package com.project.sidefit.domain.service.security;

import com.project.sidefit.advice.exception.CEmailLoginFailedException;
import com.project.sidefit.advice.exception.CRefreshTokenException;
import com.project.sidefit.advice.exception.CTokenNotFound;
import com.project.sidefit.advice.exception.CUserNotFoundException;
import com.project.sidefit.config.security.JwtProvider;
import com.project.sidefit.domain.entity.ConfirmationToken;
import com.project.sidefit.domain.entity.RefreshToken;
import com.project.sidefit.domain.entity.User;
import com.project.sidefit.domain.entity.UserPrev;
import com.project.sidefit.domain.repository.ConfirmationTokenJpaRepo;
import com.project.sidefit.domain.repository.RefreshTokenJpaRepo;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.repository.UserPrevJpaRepo;
import com.project.sidefit.domain.service.dto.TokenDto;
import com.project.sidefit.domain.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SignService {

    private final UserJpaRepo userJpaRepo;
    private final MailService mailService;
    private final UserPrevJpaRepo userPrevJpaRepo;
    private final ConfirmationTokenJpaRepo confirmationTokenJpaRepo;
    private final RefreshTokenJpaRepo refreshTokenJpaRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 해당 email을 가진 user가 존재하는지? email 중복 체크
     *
     * @return true: 중복, false: 중복X
     */
    public boolean validateDuplicatedEmail(String email) {

        // User 테이블, 임시 테이블 모두 같은 이메일이 없어야 하지 않는지
        return userJpaRepo.existsByEmail(email);
    }

    public boolean validateDuplicatedNickname(String nickname) {
        return userJpaRepo.existsByNickname(nickname);
    }


    @Transactional
    public Long saveUserPrev(String email, String password) {

        String encodedPassword = passwordEncoder.encode(password);

        UserPrev userPrev = UserPrev.createUserPrev(email, encodedPassword);
        UserPrev savedUserPrev = userPrevJpaRepo.save(userPrev);

        return savedUserPrev.getId();
    }

    // 인증 메일 전송
    @Transactional
    public void sendAuthEmail(String receiveEmail) {

        // email 로 인증토큰 생성
        ConfirmationToken confirmationToken = ConfirmationToken.createEmailConfirmationToken(receiveEmail);
        confirmationTokenJpaRepo.save(confirmationToken);

        // 인증토큰 링크 메일 전송
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(receiveEmail);
        mailMessage.setSubject("sidefit 회원가입 이메일 인증");

        // ~~~/uuid 형태
        // TODO EC2 ip 로 변경
        mailMessage.setText("http://localhost:8080/api/auth/confirm-email/" + confirmationToken.getToken());

        mailService.sendMail(mailMessage);
    }

    @Transactional
    public void sendAuthEmailAgain(String receiveEmail) {

        // TODO 예외 처리 >> 토큰 없으면 새로 생성하도록?
        ConfirmationToken confirmationToken = confirmationTokenJpaRepo.findByEmail(receiveEmail).orElseThrow(() -> new RuntimeException("해당 토큰 없음"));
        confirmationToken.updateToken();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(receiveEmail);
        mailMessage.setSubject("sidefit 회원가입 이메일 인증");

        mailMessage.setText("http://localhost:8080/api/auth/confirm-email/" + confirmationToken.getToken());
        mailService.sendMail(mailMessage);
    }

    // 토큰 검증
    @Transactional
    public void confirmEmail(String token) {
        // 넘어온 uuid 값 >> ConfirmationToken 의 pk
        // pk, 현재 시간, 만료여부 로 토큰 조회
        // 토큰 없는 경우 예외 >> 인증 메일 재전송 알림
        ConfirmationToken confirmationToken = confirmationTokenJpaRepo.findByTokenAndExpirationAfterAndExpired(token, LocalDateTime.now(), false).orElseThrow(CTokenNotFound::new);

        // 토큰 useToken() 처리
        confirmationToken.useToken();

        // TODO 토큰 삭제처리?

        // UserPrev 의 enable = true 로 변경
        // TODO orElseThrow() 에 예외 넣기
        UserPrev userPrev = userPrevJpaRepo.findByEmailAndEnable(confirmationToken.getEmail(), false).orElseThrow();
        userPrev.confirmSuccess();
    }

    // 이메일 인증 여부 확인
    public boolean checkEmailAuth(String email) {

        return userPrevJpaRepo.existsByEmailAndEnable(email, true);
    }

    @Transactional
    public void join(String email, String nickname, String job) {

        // TODO orElseThrow() 에 예외 넣기
        UserPrev userPrev = userPrevJpaRepo.findByEmailAndEnable(email, true).orElseThrow();

        // 넘어온 데이터 + email이 일치하는 userPrev 조합으로 User 생성
        User user = User.createUser(email, userPrev.getPassword(), nickname, job);

        // User 저장
        userJpaRepo.save(user);

        // UserPrev 삭제
        userPrevJpaRepo.delete(userPrev);
    }

    @Transactional
    public TokenDto login(String email, String password) {
        User user = userJpaRepo.findByEmail(email).orElseThrow(CEmailLoginFailedException::new);

        // 회원 패스워드 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CEmailLoginFailedException();
        }

        TokenDto tokenDto = jwtProvider.createTokenDto(user.getId(), user.getRoles());

        // TODO RefreshToken 은 Redis 에 보관?
        // RefreshToken 레포지토리에 저장
        RefreshToken refreshToken = RefreshToken.createRefreshToken(user.getId(), tokenDto.getRefreshToken());
        refreshTokenJpaRepo.save(refreshToken);

        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(String accessToken, String refreshToken) {

        if (!jwtProvider.validationToken(refreshToken)) {
            throw new CRefreshTokenException();
        }

        String userPk = jwtProvider.getAuthentication(accessToken).getName();
        User user = userJpaRepo.findById(Long.parseLong(userPk)).orElseThrow(CUserNotFoundException::new);

        RefreshToken token = refreshTokenJpaRepo.findByKey(user.getId()).orElseThrow(CRefreshTokenException::new);

        if (!token.getToken().equals(refreshToken)) {
            throw new CRefreshTokenException();
        }

        TokenDto tokenDto = jwtProvider.createTokenDto(user.getId(), user.getRoles());
        token.updateToken(tokenDto.getRefreshToken());

        return tokenDto;
    }
}
