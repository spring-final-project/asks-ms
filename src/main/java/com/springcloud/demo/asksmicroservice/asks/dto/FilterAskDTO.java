package com.springcloud.demo.asksmicroservice.asks.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterAskDTO {
    @UUID
    String roomId;

    @UUID
    String userId;

    @PositiveOrZero
    @Builder.Default
    Integer page = 1;

    @PositiveOrZero
    @Builder.Default
    Integer limit = 20;
}
