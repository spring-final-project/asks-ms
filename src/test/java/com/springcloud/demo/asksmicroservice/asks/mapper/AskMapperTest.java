package com.springcloud.demo.asksmicroservice.asks.mapper;

import com.springcloud.demo.asksmicroservice.asks.dto.CreateAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.ResponseAskDTO;
import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

class AskMapperTest {

    @Test
    void createAskDtoToAsk(){
        CreateAskDTO createAskDTO = CreateAskDTO
                .builder()
                .question("First question")
                .roomId(UUID.randomUUID().toString())
                .build();

        Ask response = AskMapper.createAskDtoToAsk(createAskDTO);

        assertThat(response.getQuestion()).isEqualTo(createAskDTO.getQuestion());
        assertThat(response.getRoomId()).isEqualTo(createAskDTO.getRoomId());
    }

    @Test
    void askToResponseAskDto(){
        Ask ask = Ask
                .builder()
                .id(UUID.randomUUID().toString())
                .question("First question")
                .answer("First answer")
                .createdAt(LocalDateTime.now())
                .respondedAt(LocalDateTime.now())
                .roomId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .build();

        ResponseAskDTO response = AskMapper.askToResponseAskDto(ask);

        assertThat(response.getId()).isEqualTo(ask.getId());
        assertThat(response.getQuestion()).isEqualTo(ask.getQuestion());
        assertThat(response.getAnswer()).isEqualTo(ask.getAnswer());
        assertThat(response.getCreatedAt()).isEqualTo(ask.getCreatedAt().toString());
        assertThat(response.getRespondedAt()).isEqualTo(ask.getRespondedAt().toString());
        assertThat(response.getRoomId()).isEqualTo(ask.getRoomId());
        assertThat(response.getUserId()).isEqualTo(ask.getUserId());
    }
}