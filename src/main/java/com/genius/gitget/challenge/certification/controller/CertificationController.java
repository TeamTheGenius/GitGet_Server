package com.genius.gitget.challenge.certification.controller;

import static com.genius.gitget.global.util.exception.SuccessCode.SUCCESS;

import com.genius.gitget.challenge.certification.dto.CertificationRequest;
import com.genius.gitget.challenge.certification.dto.CertificationResponse;
import com.genius.gitget.challenge.certification.dto.GithubTokenRequest;
import com.genius.gitget.challenge.certification.dto.PullRequestResponse;
import com.genius.gitget.challenge.certification.dto.RepositoryRequest;
import com.genius.gitget.challenge.certification.service.CertificationService;
import com.genius.gitget.challenge.user.service.UserService;
import com.genius.gitget.global.security.domain.UserPrincipal;
import com.genius.gitget.global.util.response.dto.CommonResponse;
import com.genius.gitget.global.util.response.dto.ListResponse;
import com.genius.gitget.global.util.response.dto.SingleResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/certification")
public class CertificationController {
    private final CertificationService certificationService;
    private final UserService userService;

    @PostMapping("/register/token")
    public ResponseEntity<CommonResponse> registerGithubToken(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody GithubTokenRequest githubTokenRequest
    ) {
        certificationService.registerGithubPersonalToken(userPrincipal.getUser(), githubTokenRequest.githubToken());

        return ResponseEntity.ok().body(
                new CommonResponse(SUCCESS.getStatus(), SUCCESS.getMessage())
        );
    }

    @PostMapping("/register/repository")
    public ResponseEntity<CommonResponse> registerRepository(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody RepositoryRequest repositoryRequest
    ) {

        certificationService.registerRepository(userPrincipal.getUser(), repositoryRequest.instanceId(),
                repositoryRequest.repositoryName());

        return ResponseEntity.ok().body(
                new CommonResponse(SUCCESS.getStatus(), SUCCESS.getMessage())
        );
    }

    @GetMapping("/verify/{instanceId}")
    public ResponseEntity<ListResponse<PullRequestResponse>> verify(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long instanceId
    ) {

        List<PullRequestResponse> pullRequestResponses = certificationService.getPullRequestListByDate(
                userPrincipal.getUser(), instanceId, LocalDate.now());

        return ResponseEntity.ok().body(
                new ListResponse<>(SUCCESS.getStatus(), SUCCESS.getMessage(), pullRequestResponses)
        );
    }

    @PostMapping("/today")
    public ResponseEntity<SingleResponse<CertificationResponse>> certificateByGithub(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CertificationRequest certificationRequest
    ) {

        CertificationResponse certificationResponse = certificationService.updateCertification(
                userPrincipal.getUser(),
                certificationRequest);

        return ResponseEntity.ok().body(
                new SingleResponse<>(SUCCESS.getStatus(), SUCCESS.getMessage(), certificationResponse)
        );
    }
}
