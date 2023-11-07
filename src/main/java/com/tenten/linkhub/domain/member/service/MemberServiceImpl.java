package com.tenten.linkhub.domain.member.service;

import com.tenten.linkhub.domain.auth.JwtProvider;
import com.tenten.linkhub.domain.member.model.FavoriteCategory;
import com.tenten.linkhub.domain.member.model.Member;
import com.tenten.linkhub.domain.member.model.ProfileImage;
import com.tenten.linkhub.domain.member.model.Provider;
import com.tenten.linkhub.domain.member.model.Role;
import com.tenten.linkhub.domain.member.repository.MemberEmailRedisRepository;
import com.tenten.linkhub.domain.member.repository.MemberRepository;
import com.tenten.linkhub.domain.member.service.dto.MailVerificationRequest;
import com.tenten.linkhub.domain.member.service.dto.MailVerificationResponse;
import com.tenten.linkhub.domain.member.service.dto.MemberAuthInfo;
import com.tenten.linkhub.domain.member.service.dto.MemberFindResponse;
import com.tenten.linkhub.domain.member.service.dto.MemberInfos;
import com.tenten.linkhub.domain.member.service.dto.MemberJoinRequest;
import com.tenten.linkhub.domain.member.service.dto.MemberJoinResponse;
import com.tenten.linkhub.global.aws.dto.ImageInfo;
import com.tenten.linkhub.global.aws.dto.ImageSaveRequest;
import com.tenten.linkhub.global.aws.s3.S3Uploader;
import com.tenten.linkhub.global.exception.UnauthorizedAccessException;
import com.tenten.linkhub.global.infrastructure.ses.AwsSesService;
import com.tenten.linkhub.global.util.email.EmailDto;
import com.tenten.linkhub.global.util.email.VerificationCodeCreator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private static final String MEMBER_IMAGE_FOLDER = "member-image/";
    private static final String MEMBER_DEFAULT_IMAGE_PATH = "https://team-10-bucket.s3.ap-northeast-2.amazonaws.com/member-image/member-default.png";

    private final MemberRepository memberRepository;
    private final AwsSesService emailService;
    private final VerificationCodeCreator verificationCodeCreator;
    private final MemberEmailRedisRepository memberEmailRedisRepository;
    private final S3Uploader s3Uploader;
    private final JwtProvider jwtProvider;

    public MemberServiceImpl(MemberRepository memberRepository,
            AwsSesService emailService,
                             VerificationCodeCreator verificationCodeCreator,
            MemberEmailRedisRepository memberEmailRedisRepository, S3Uploader s3Uploader, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.emailService = emailService;
        this.verificationCodeCreator = verificationCodeCreator;
        this.memberEmailRedisRepository = memberEmailRedisRepository;
        this.s3Uploader = s3Uploader;
        this.jwtProvider = jwtProvider;
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

    @Override
    public MemberInfos findMemberInfosByMemberIds(List<Long> memberIds) {
        List<Member> members = memberRepository.findMemberWithProfileImageByMemberIds(memberIds);

        return MemberInfos.from(members);
    }

    @Transactional
    @Override
    public MemberFindResponse findMember(String socialId, Provider provider) {
        Optional<Member> member = memberRepository.findBySocialIdAndProvider(socialId, provider);

        return MemberFindResponse.from(member);
    }

    @Transactional
    @Override
    public MemberJoinResponse join(MemberJoinRequest memberJoinRequest) {
        memberRepository.findBySocialIdAndProvider(memberJoinRequest.socialId(), memberJoinRequest.provider())
                .ifPresent(m -> {
                    throw new UnauthorizedAccessException("이미 가입한 회원입니다.");
                });

        ImageInfo imageInfo = getNewImageInfoOrDefaultImageInfo(memberJoinRequest.file());

        Member member = new Member(
                memberJoinRequest.socialId(),
                memberJoinRequest.provider(),
                Role.USER,
                memberJoinRequest.nickname(),
                memberJoinRequest.aboutMe(),
                memberJoinRequest.newsEmail(),
                memberJoinRequest.isSubscribed(),
                new ProfileImage(imageInfo.path(), imageInfo.name()),
                new FavoriteCategory(memberJoinRequest.favoriteCategory())
        );

        Member savedMember = memberRepository.save(member);

        String jwt = jwtProvider.generateTokenFromMember(MemberAuthInfo.from(savedMember));

        return MemberJoinResponse.from(jwt);
    }

    private ImageInfo getNewImageInfoOrDefaultImageInfo(MultipartFile file) {
        if (file == null) {
            return ImageInfo.of(MEMBER_DEFAULT_IMAGE_PATH, "default-image");
        }

        ImageSaveRequest imageSaveRequest = ImageSaveRequest.of(file, MEMBER_IMAGE_FOLDER);
        return s3Uploader.saveImage(imageSaveRequest);
    }

}
