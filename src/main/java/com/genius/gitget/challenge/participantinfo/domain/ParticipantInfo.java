package com.genius.gitget.challenge.participantinfo.domain;

import com.genius.gitget.challenge.certification.domain.Certification;
import com.genius.gitget.challenge.instance.domain.Instance;
import com.genius.gitget.challenge.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "participantInfo")
public class ParticipantInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participantInfo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    private Instance instance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "participantInfo")
    private List<Certification> certificationList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "join_status")
    @NotNull
    @ColumnDefault("'YES'")
    private JoinStatus joinStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_result")
    private JoinResult joinResult;

    private String repositoryName;

    @Builder
    public ParticipantInfo(JoinStatus joinStatus, JoinResult joinResult, String repositoryName) {
        this.joinStatus = joinStatus;
        this.joinResult = joinResult;
        this.repositoryName = repositoryName;
    }

    public static ParticipantInfo createDefaultParticipantInfo(String repositoryName) {
        return ParticipantInfo.builder()
                .joinStatus(JoinStatus.YES)
                .joinResult(JoinResult.PROCESSING)
                .repositoryName(repositoryName)
                .build();
    }

    //=== 비지니스 로직 ===//
    public void quitInstance() {
        this.joinStatus = JoinStatus.NO;
        this.joinResult = JoinResult.FAIL;
    }

    public void updateRepository(String repository) {
        this.repositoryName = repository;
    }

    public LocalDate getStartedDate() {
        return this.getInstance().getStartedDate().toLocalDate();
    }


    /*== 연관관계 편의 메서드 ==*/
    public void setUserAndInstance(User user, Instance instance) {
        addParticipantInfoForUser(user);
        addParticipantInfoForInstance(instance);
    }

    private void addParticipantInfoForUser(User user) {
        this.user = user;
        if (!(user.getParticipantInfoList().contains(this))) {
            user.getParticipantInfoList().add(this);
        }
    }

    private void addParticipantInfoForInstance(Instance instance) {
        this.instance = instance;
        if (!(instance.getParticipantInfoList().contains(this))) {
            instance.getParticipantInfoList().add(this);
        }
    }
}
