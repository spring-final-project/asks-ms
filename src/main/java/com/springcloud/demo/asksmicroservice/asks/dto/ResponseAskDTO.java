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

    String id;
    String createdAt;
    String respondedAt;
    String question;
    String answer;
    String roomId;
    String userId;
}
