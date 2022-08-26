package com.project.sidefit.domain.service;

import com.project.sidefit.advice.exception.CTokenNotFound;
import com.project.sidefit.advice.exception.CUserNotFoundException;
import com.project.sidefit.domain.entity.*;
import com.project.sidefit.domain.repository.ConfirmationTokenJpaRepo;
import com.project.sidefit.domain.repository.UserJpaRepo;
import com.project.sidefit.domain.service.dto.UserDetailDto;
import com.project.sidefit.domain.service.dto.UserDto;
import com.project.sidefit.domain.service.dto.UserListDto;
import com.project.sidefit.domain.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserJpaRepo userJpaRepo;
    private final ConfirmationTokenJpaRepo confirmationTokenJpaRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    // TODO List가 아닌 page? slice?
    public List<UserListDto> findAll() {
        List<User> users = userJpaRepo.findAll();

        return users.stream().map(u -> new UserListDto(u)).collect(Collectors.toList());
    }

    public UserDetailDto findDetail(Long id) {

        User user = userJpaRepo.findById(id).orElseThrow(CUserNotFoundException::new);
        return new UserDetailDto(user);
    }

    // TODO 쿼리가 이상함
    @Transactional
    public Long save(User user1) {
        User user = userJpaRepo.findById(user1.getId()).get();
        user.getTags().add(new Tag("tag1"));
        user.getTags().add(new Tag("tag2"));
        user.getFavorites().add(new Favorite("favorite"));
        user.getCurrentStatuses().add(new CurrentStatus("status"));
        user.getTeches().add(new Tech("tech"));
        user.updateMbti(Mbti.INFP);

        return user.getId();
    }

    @Transactional
    public void sendPasswordEmail(String receiveEmail) {
        ConfirmationToken confirmationToken = ConfirmationToken.createEmailConfirmationToken(receiveEmail);
        confirmationTokenJpaRepo.save(confirmationToken);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(receiveEmail);
        mailMessage.setSubject("sidefit 비밀번호 변경 안내");

        // TODO 프론트쪽이랑 회의, 해당 링크 pathParamter 나 request paramter 에 token 전달하면 해당 토큰 그대로 인증 url에 더해서?
        mailMessage.setText("pw 변경화면으로 가는 링크" + confirmationToken.getToken());

        mailService.sendMail(mailMessage);
    }

    @Transactional
    public void updatePassword(String token, String password) {
        ConfirmationToken confirmationToken = confirmationTokenJpaRepo.findByTokenAndExpirationAfterAndExpired(token, LocalDateTime.now(), false).orElseThrow(CTokenNotFound::new);

        confirmationToken.useToken();

        // TODO 사용된 토큰 삭제처리?
        User user = userJpaRepo.findByEmail(confirmationToken.getEmail()).orElseThrow(IllegalStateException::new);
        user.updatePassword(passwordEncoder.encode(password));
    }

    @Transactional
    public void updateUser(Long id, UserDto userDto) {
        User user = userJpaRepo.findById(id).orElseThrow(RuntimeException::new);

        List<Tag> tags = userDto.getTags().stream().map(tag -> new Tag(tag)).collect(Collectors.toList());
        List<Favorite> favorites = userDto.getFavorites().stream().map(favorite -> new Favorite(favorite)).collect(Collectors.toList());
        List<CurrentStatus> currentStatuses = userDto.getCurrentStatuses().stream().map(status -> new CurrentStatus(status)).collect(Collectors.toList());
        List<Tech> teches = userDto.getTeches().stream().map(tech -> new Tech(tech)).collect(Collectors.toList());

        user.updateUser(userDto.getJob(), userDto.getIntroduction(), tags, favorites, currentStatuses, teches, userDto.getMbti());
    }
}
