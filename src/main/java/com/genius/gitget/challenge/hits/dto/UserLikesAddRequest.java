package com.genius.gitget.challenge.hits.dto;

import lombok.Data;

@Data
public class UserLikesAddRequest {
    private String identifier;
    private Long instanceId;

    public UserLikesAddRequest(String identifier, Long instanceId) {
        this.identifier = identifier;
        this.instanceId = instanceId;
    }
}
