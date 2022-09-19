package com.project.sidefit.domain.service;

import com.project.sidefit.advice.exception.CTokenNotFound;
import com.project.sidefit.advice.exception.CUserNotFoundException;
import com.project.sidefit.domain.entity.*;

import com.project.sidefit.domain.entity.user.User;
import com.project.sidefit.domain.repository.ConfirmationTokenRepository;
import com.project.sidefit.domain.repository.ImageRepository;
import com.project.sidefit.domain.repository.user.UserRepository;
import com.project.sidefit.domain.service.dto.UserDetailDto;
import com.project.sidefit.domain.service.dto.UserDto;
import com.project.sidefit.domain.service.dto.UserListDto;
import com.project.sidefit.domain.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userJpaRepo;
    private final ConfirmationTokenRepository confirmationTokenJpaRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    // TODO List가 아닌 page? slice?
    public List<UserListDto> findAll() {
        List<User> users = userJpaRepo.findAll();

        return users.stream().map(u -> new UserListDto(u)).collect(Collectors.toList());
    }

    // TODO user, image fetch join?
    public UserDetailDto findDetail(Long id) {

        User user = userJpaRepo.findById(id).orElseThrow(CUserNotFoundException::new);
        Image image = user.getImage();
        String imageUrl = null;
        if (image != null) {
            imageUrl = image.getImageUrl();
        }
        return new UserDetailDto(user, imageUrl);
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

    // TODO 쿼리정리 >> 쿼리가 너무 많이 나감
    @Transactional
    public void updateUser(Long id, MultipartFile image, UserDto userDto) throws IOException {
        User user = userJpaRepo.findById(id).orElseThrow(RuntimeException::new);

        List<Tag> tags = userDto.getTags().stream().map(tag -> new Tag(tag)).collect(Collectors.toList());
        List<Favorite> favorites = userDto.getFavorites().stream().map(favorite -> new Favorite(favorite)).collect(Collectors.toList());
        List<CurrentStatus> currentStatuses = userDto.getCurrentStatuses().stream().map(status -> new CurrentStatus(status)).collect(Collectors.toList());
        List<Tech> teches = userDto.getTeches().stream().map(tech -> new Tech(tech)).collect(Collectors.toList());
        List<Portfolio> portfolios = userDto.getPortfolios().stream().map(p -> new Portfolio(p.getTitle(), p.getUrl())).collect(Collectors.toList());

        String imageUrl = s3Service.uploadFiles(image, user.getEmail());
        Image findImage = imageRepository.findByImageUrl(imageUrl).orElseThrow(IllegalStateException::new);

        user.updateUser(userDto.getJob(), userDto.getIntroduction(), tags, favorites, currentStatuses,
                teches, portfolios, userDto.getMbti(), findImage);
    }
}
