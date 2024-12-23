package com.springcloud.demo.asksmicroservice.asks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAskDTO {
    private String id;
    private String createdAt;
    private String respondedAt;
    private String question;
    private String answer;
    private String roomId;
    private String userId;
}
