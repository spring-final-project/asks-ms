package com.springcloud.demo.asksmicroservice.asks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerAskDTO {

    @NotBlank
    @Size(min = 1, max = 255)
    String answer;
}
