package com.springcloud.demo.asksmicroservice.asks.mapper;

import com.springcloud.demo.asksmicroservice.asks.dto.CreateAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.ResponseAskDTO;
import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import org.springframework.stereotype.Component;

@Component
public class AskMapper {

    public static Ask createAskDtoToAsk(CreateAskDTO createAskDTO){
        return Ask
                .builder()
                .question(createAskDTO.getQuestion())
                .roomId(createAskDTO.getRoomId())
                .build();
    }

    public static ResponseAskDTO askToResponseAskDto(Ask ask){
        ResponseAskDTO responseAskDTO = ResponseAskDTO
                .builder()
                .id(ask.getId())
                .createdAt(ask.getCreatedAt().toString())
                .answer(ask.getAnswer())
                .question(ask.getQuestion())
                .roomId(ask.getRoomId())
                .userId(ask.getUserId())
                .build();

        if(ask.getRespondedAt() != null){
            responseAskDTO.setRespondedAt(ask.getRespondedAt().toString());
        }

        return responseAskDTO;
    }
}
