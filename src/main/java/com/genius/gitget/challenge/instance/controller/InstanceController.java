package com.genius.gitget.challenge.instance.controller;

import com.genius.gitget.challenge.instance.dto.crud.InstanceCreateRequest;
import com.genius.gitget.challenge.instance.dto.crud.InstanceDetailResponse;
import com.genius.gitget.challenge.instance.dto.crud.InstancePagingResponse;
import com.genius.gitget.challenge.instance.dto.crud.InstanceUpdateRequest;
import com.genius.gitget.challenge.instance.service.InstanceService;
import com.genius.gitget.global.util.exception.SuccessCode;
import com.genius.gitget.global.util.response.dto.CommonResponse;
import com.genius.gitget.global.util.response.dto.PagingResponse;
import com.genius.gitget.global.util.response.dto.SingleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/instance")
@RequiredArgsConstructor
public class InstanceController {
    private final InstanceService instanceService;

    // 인스턴스 리스트 조회
    @GetMapping
    public ResponseEntity<PagingResponse<InstancePagingResponse>> getAllInstances (
            @PageableDefault(size = 5, direction = Sort.Direction.ASC, sort = "id") Pageable pageable) throws IOException{
        Page<InstancePagingResponse> instances = instanceService.getAllInstances(pageable);

        return ResponseEntity.ok().body(
                new PagingResponse<>(SuccessCode.SUCCESS.getStatus(), SuccessCode.SUCCESS.getMessage(), instances)
        );
    }

    // 인스턴스 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<SingleResponse<InstanceDetailResponse>> getInstanceById(@PathVariable Long id) throws IOException{
        InstanceDetailResponse instanceDetails = instanceService.getInstanceById(id);
        return ResponseEntity.ok().body(
                new SingleResponse<>(SuccessCode.SUCCESS.getStatus(), SuccessCode.SUCCESS.getMessage(), instanceDetails)
        );
    }

    // 인스턴스 생성
    @PostMapping
    public ResponseEntity<CommonResponse> createInstance(
            @RequestPart InstanceCreateRequest instanceCreateRequest,
            @RequestPart(value = "files", required = false) MultipartFile multipartFile, @RequestPart(value = "type") String type) throws IOException {
        instanceService.createInstance(instanceCreateRequest, multipartFile, type);
        return ResponseEntity.ok().body(
                new CommonResponse(SuccessCode.SUCCESS.getStatus(), SuccessCode.CREATED.getMessage())
        );
    }

    // 인스턴스 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse> updateInstance(@PathVariable Long id, @RequestPart InstanceUpdateRequest instanceUpdateRequest,
                                                         @RequestPart(value = "files", required = false) MultipartFile multipartFile, @RequestPart(value = "type") String type) throws IOException{
        instanceService.updateInstance(id, instanceUpdateRequest, multipartFile, type);
        return ResponseEntity.ok().body(
                new CommonResponse(SuccessCode.SUCCESS.getStatus(), SuccessCode.SUCCESS.getMessage())
        );
    }

    // 인스턴스 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse> deleteInstance(@PathVariable Long id) throws IOException{
        instanceService.deleteInstance(id);
        return ResponseEntity.ok().body(
                new CommonResponse(SuccessCode.SUCCESS.getStatus(), SuccessCode.SUCCESS.getMessage())
        );
    }
}