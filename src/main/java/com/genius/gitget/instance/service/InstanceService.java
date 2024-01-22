package com.genius.gitget.instance.service;

import com.genius.gitget.instance.dto.InstanceCreateRequest;
import com.genius.gitget.instance.dto.InstanceDetailResponse;
import com.genius.gitget.instance.dto.InstancePagingResponse;
import com.genius.gitget.instance.dto.InstanceUpdateRequest;
import com.genius.gitget.topic.domain.Topic;
import com.genius.gitget.util.exception.BusinessException;
import com.genius.gitget.instance.domain.Instance;
import com.genius.gitget.instance.repository.InstanceRepository;
import com.genius.gitget.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.genius.gitget.util.exception.ErrorCode.INSTANCE_NOT_FOUND;
import static com.genius.gitget.util.exception.ErrorCode.TOPIC_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final TopicRepository topicRepository;

    // 인스턴스 생성
    @Transactional
    public void createInstance(InstanceCreateRequest instanceCreateRequest) {
        Topic topic = topicRepository.findById(instanceCreateRequest.topicId())
                .orElseThrow(() -> new BusinessException(TOPIC_NOT_FOUND));

        Instance instance = Instance.builder()
                .description(instanceCreateRequest.description())
                .pointPerPerson(instanceCreateRequest.pointPerPerson())
                .startedDate(instanceCreateRequest.startedAt())
                .completedDate(instanceCreateRequest.completedAt())
                .build();

        instance.setTopic(topic);

        instanceRepository.save(instance);
    }


    public Page<InstancePagingResponse> getAllInstances(Pageable pageable) {
        Page<Instance> instances = instanceRepository.findAllById(pageable);
        return instances.map(instance -> new InstancePagingResponse(instance.getTopic().getId(), instance.getId(), instance.getTitle(), instance.getStartedDate(), instance.getCompletedDate()));

    }

    // 인스턴스 단건 조회
    public InstanceDetailResponse getInstanceById(Long id) {
        Instance instanceDetails = instanceRepository.findById(id).orElseThrow(() -> new BusinessException(INSTANCE_NOT_FOUND));
        return new InstanceDetailResponse(
                instanceDetails.getTopic().getId(),
                instanceDetails.getId(),
                instanceDetails.getTitle(),
                instanceDetails.getDescription(),
                instanceDetails.getPointPerPerson(),
                instanceDetails.getTags(),
                instanceDetails.getStartedDate(),
                instanceDetails.getCompletedDate()
        );
    }

    @Transactional
    public void deleteInstance(Long id) {
        Instance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(INSTANCE_NOT_FOUND));
        instanceRepository.delete(instance);
    }

    // 인스턴스 수정
    @Transactional
    public void updateInstance(Long id, InstanceUpdateRequest instanceUpdateRequest) {
        Instance existingInstance = instanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(INSTANCE_NOT_FOUND));

        existingInstance.updateInstance(instanceUpdateRequest.description(), instanceUpdateRequest.pointPerPerson(),
                instanceUpdateRequest.startedAt(), instanceUpdateRequest.completedAt());

        instanceRepository.save(existingInstance);
    }
}
