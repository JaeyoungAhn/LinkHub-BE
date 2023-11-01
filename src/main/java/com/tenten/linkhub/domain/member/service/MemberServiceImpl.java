package com.tenten.linkhub.domain.member.service;

import com.tenten.linkhub.domain.member.model.Member;
import com.tenten.linkhub.domain.member.model.Provider;
import com.tenten.linkhub.domain.member.repository.MemberEmailRedisRepository;
import com.tenten.linkhub.domain.member.repository.MemberJpaRepository;
import com.tenten.linkhub.domain.member.service.dto.MailVerificationRequest;
import com.tenten.linkhub.domain.member.service.dto.MailVerificationResponse;
import com.tenten.linkhub.global.infrastructure.ses.AwsSesService;
import com.tenten.linkhub.global.util.email.EmailDto;
import com.tenten.linkhub.global.util.email.VerificationCodeCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final AwsSesService emailService;
    private final VerificationCodeCreator verificationCodeCreator;
    private final MemberEmailRedisRepository memberEmailRedisRepository;
    private final MemberJpaRepository memberJpaRepository;

    public MemberServiceImpl(AwsSesService emailService,
                             VerificationCodeCreator verificationCodeCreator,
                             MemberEmailRedisRepository memberEmailRedisRepository,
            MemberJpaRepository memberJpaRepository) {
        this.emailService = emailService;
        this.verificationCodeCreator = verificationCodeCreator;
        this.memberEmailRedisRepository = memberEmailRedisRepository;
        this.memberJpaRepository = memberJpaRepository;
    }

    @Transactional
    @Override
    public void sendVerificationEmail(EmailDto emailDto) {
        final String authKey = verificationCodeCreator.createVerificationCode();
        emailService.sendVerificationCodeEmail(emailDto, authKey);
        memberEmailRedisRepository.saveExpire(authKey, emailDto.getTo().get(0), 60 * 3L);
    }

    @Override
    public MailVerificationResponse verificateEmail(MailVerificationRequest request) {
        //todo : 이메일 중복 관련 기획 로직 추가 구현 고려해야함.

        String emailFoundByCode = memberEmailRedisRepository.getEmail(request.code());
        if (emailFoundByCode == null || !emailFoundByCode.equals(request.email())) {
            return new MailVerificationResponse(false);
        }

        return new MailVerificationResponse(true);
    }

    @Transactional
    @Override
    public Long findOrCreateUser(String socialId, String provider) {
        Member user = memberJpaRepository.findBySocialIdAndProvider(socialId, provider)
                .orElseGet(() -> {
                    Member newUser = new Member(socialId, Provider.valueOf(provider));
                    return memberJpaRepository.save(newUser);
                });

        return user.getId();
    }


}
