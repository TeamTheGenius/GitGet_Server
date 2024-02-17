package com.genius.gitget.challenge.certification.service;

import static com.genius.gitget.challenge.certification.domain.CertificateStatus.CERTIFICATED;
import static com.genius.gitget.challenge.certification.domain.CertificateStatus.NOT_YET;

import com.genius.gitget.challenge.certification.domain.CertificateStatus;
import com.genius.gitget.challenge.certification.domain.Certification;
import com.genius.gitget.challenge.certification.dto.CertificationResponse;
import com.genius.gitget.challenge.certification.dto.CertificationStatus;
import com.genius.gitget.challenge.certification.dto.RenewRequest;
import com.genius.gitget.challenge.certification.dto.RenewResponse;
import com.genius.gitget.challenge.certification.repository.CertificationRepository;
import com.genius.gitget.challenge.certification.util.DateUtil;
import com.genius.gitget.challenge.instance.domain.Instance;
import com.genius.gitget.challenge.instance.service.InstanceService;
import com.genius.gitget.challenge.participantinfo.domain.ParticipantInfo;
import com.genius.gitget.challenge.participantinfo.service.ParticipantInfoService;
import com.genius.gitget.challenge.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CertificationService {
    private final GithubProvider githubProvider;
    private final InstanceService instanceService;
    private final ParticipantInfoService participantInfoService;
    private final CertificationRepository certificationRepository;


    public List<RenewResponse> getWeekCertification(Long participantInfoId, LocalDate currentDate) {
        LocalDate startDate = currentDate.minusDays(currentDate.getDayOfWeek().ordinal());
        List<Certification> certifications = certificationRepository.findCertificationByDuration(startDate, currentDate,
                participantInfoId);

        //TODO: 중간에 인증이 안된 부분이 있을 때 더미 데이터를 끼워주는게 맞을듯
        return certifications.stream()
                .map(RenewResponse::create)
                .toList();
    }

    public List<RenewResponse> getTotalCertification(Long participantInfoId, LocalDate currentDate) {
        ParticipantInfo participantInfo = participantInfoService.findParticipantInfoById(participantInfoId);
        Instance instance = participantInfo.getInstance();

        List<Certification> certifications = certificationRepository.findCertificationByDuration(
                instance.getStartedDate().toLocalDate(), currentDate, participantInfoId);

        return certifications.stream()
                .map(RenewResponse::create)
                .toList();
    }

    @Transactional
    public RenewResponse updateCertification(User user, RenewRequest renewRequest) {
        GitHub gitHub = githubProvider.getGithubConnection(user);
        ParticipantInfo participantInfo = participantInfoService.getParticipantInfoByJoinInfo(user.getId(),
                renewRequest.instanceId());

        List<GHPullRequest> ghPullRequests = githubProvider.getPullRequestByDate(
                        gitHub,
                        participantInfo.getRepositoryName(),
                        renewRequest.targetDate())
                .nextPage();

        Certification certification = createCertification(
                participantInfo,
                renewRequest,
                ghPullRequests);

        return RenewResponse.create(certification);
    }

    private Certification createCertification(ParticipantInfo participantInfo,
                                              RenewRequest renewRequest,
                                              List<GHPullRequest> ghPullRequests) {
        int attempt = DateUtil.getDiffBetweenDate(participantInfo.getStartedDate(),
                renewRequest.targetDate());

        Certification certification = certificationRepository.findCertificationByDate(
                        renewRequest.targetDate(),
                        participantInfo.getId())
                .orElse(Certification.builder()
                        .currentAttempt(attempt)
                        .certificatedAt(renewRequest.targetDate())
                        .certificationLinks(getPrLinks(ghPullRequests))
                        .certificationStatus(getCertificateStatus(ghPullRequests))
                        .build());
        certification.setParticipantInfo(participantInfo);
        return certificationRepository.save(certification);
    }

    private String getPrLinks(List<GHPullRequest> ghPullRequests) {
        StringBuilder prLinkBuilder = new StringBuilder();
        for (GHPullRequest pullRequest : ghPullRequests) {
            prLinkBuilder.append(pullRequest.getHtmlUrl().toString());
            prLinkBuilder.append(",");
        }
        return prLinkBuilder.toString();
    }

    private CertificateStatus getCertificateStatus(List<GHPullRequest> ghPullRequests) {
        if (ghPullRequests.isEmpty()) {
            return CertificateStatus.NOT_YET;
        }
        return CERTIFICATED;
    }

    public CertificationResponse getCertificationInformation(User user, Long instanceId) {
        Instance instance = instanceService.findInstanceById(instanceId);
        ParticipantInfo participantInfo = participantInfoService.getParticipantInfoByJoinInfo(user.getId(),
                instanceId);

        return CertificationResponse.createByEntity(instance, participantInfo.getRepositoryName());
    }

    @Transactional
    public CertificationStatus getCertificationStatus(Instance instance, ParticipantInfo participantInfo,
                                                      LocalDate targetDate) {
        //성공 인증 개수
        int successCount = certificationRepository.findCertificationByStatus(
                        participantInfo.getId(), CERTIFICATED, targetDate)
                .size();
        //실패 인증 개수
        int failureCount = certificationRepository.findCertificationByStatus(
                        participantInfo.getId(), NOT_YET, targetDate)
                .size();

        //targetDate 기준 현재 진행 일차
        int currentAttempt = DateUtil.getDiffBetweenDate(instance.getStartedDate().toLocalDate(), targetDate);

        //남은 인증 개수 = 전체 일차 - 오늘 회차
        int totalAttempt = instance.getTotalAttempt();
        int remainAttempt = totalAttempt - currentAttempt;

        //성공 퍼센트
        double successPercent = (double) successCount / (double) currentAttempt * 100;
        double roundedPercent = Math.round(successPercent * 100 / 100.0);

        return CertificationStatus.builder()
                .successPercent(roundedPercent)
                .totalAttempt(totalAttempt)
                .currentAttempt(currentAttempt)
                .pointPerPerson(instance.getPointPerPerson())
                .successCount(successCount)
                .failureCount(failureCount)
                .remainCount(remainAttempt)
                .build();
    }
}
