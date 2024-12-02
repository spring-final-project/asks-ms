package com.springcloud.demo.asksmicroservice.asks.service;

import com.springcloud.demo.asksmicroservice.asks.dto.AnswerAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.CreateAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.FilterAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.ResponseAskDTO;
import com.springcloud.demo.asksmicroservice.asks.mapper.AskMapper;
import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import com.springcloud.demo.asksmicroservice.asks.repository.AskRepository;
import com.springcloud.demo.asksmicroservice.asks.repository.AskSpecification;
import com.springcloud.demo.asksmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.asksmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.asksmicroservice.client.users.UserClientImpl;
import com.springcloud.demo.asksmicroservice.client.users.UserDTO;
import com.springcloud.demo.asksmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.asksmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.asksmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.asksmicroservice.utils.JsonUtils;
import com.springcloud.demo.asksmicroservice.messaging.MessagingProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AskService {

    private final AskRepository askRepository;
    private final AskSpecification askSpecification;
    private final RoomClientImpl roomClient;
    private final UserClientImpl userClient;
    private final MessagingProducer messagingProducer;

    @Value("${spring.kafka.topics.ASK_CREATED_TOPIC}")
    private String askCreatedTopic;

    public ResponseAskDTO create(CreateAskDTO createAskDTO, String idUserLogged) {

//        Check exist room
        RoomDTO room = roomClient.findById(createAskDTO.getRoomId());
        UserDTO owner = userClient.findById(room.getOwnerId());
        room.setOwner(owner);
        UserDTO user = userClient.findById(idUserLogged);

        Ask askCreated = AskMapper.createAskDtoToAsk(createAskDTO);
        askCreated.setUserId(idUserLogged);

        askCreated = askRepository.save(askCreated);

        messagingProducer.sendMessage(askCreatedTopic, JsonUtils.toJson(AskMapper.askToPublishAskEventDto(askCreated,room,user)));

        return AskMapper.askToResponseAskDto(askCreated);
    }

    public List<ResponseAskDTO> findAll(FilterAskDTO filters) {
        Pageable pagination = PageRequest.of(filters.getPage() - 1, filters.getLimit());
        List<Ask> asks = askRepository.findAll(askSpecification.withFilters(filters), pagination).getContent();

        return asks.stream().map(AskMapper::askToResponseAskDto).toList();
    }

    public ResponseAskDTO findById(String id) {
        Ask ask = askRepository.findById(id).orElseThrow(()-> new NotFoundException("Not found ask with id: " + id));

        return AskMapper.askToResponseAskDto(ask);
    }

    public ResponseAskDTO answer(String id, AnswerAskDTO answerAskDTO, String idUserLogged) {
        Ask ask = askRepository.findById(id).orElseThrow(()-> new NotFoundException("Not found ask with id: " + id));

        // Check if user logged is owner of room
        RoomDTO roomOfAsk = roomClient.findById(ask.getRoomId());
        if(!roomOfAsk.getOwnerId().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to answer ask or room that belong to another user");
        }

        ask.setAnswer(answerAskDTO.getAnswer());

        ask = askRepository.save(ask);

        return AskMapper.askToResponseAskDto(ask);
    }

    public SimpleResponseDTO delete(String id, String idUserLogged) {
        Ask ask = askRepository.findById(id).orElseThrow(()-> new NotFoundException("Not found ask with id: " + id));

        // Check if user logged is owner of room
        RoomDTO roomOfAsk = roomClient.findById(ask.getRoomId());
        if(!roomOfAsk.getOwnerId().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to answer ask or room that belong to another user");
        }

        askRepository.delete(ask);

        return new SimpleResponseDTO(true);
    }

    public ResponseAskDTO deleteAnswer(String id, String idUserLogged) {
        Ask ask = askRepository.findById(id).orElseThrow(()-> new NotFoundException("Not found ask with id: " + id));

        // Check if user logged is owner of room
        RoomDTO roomOfAsk = roomClient.findById(ask.getRoomId());
        if(!roomOfAsk.getOwnerId().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to answer ask or room that belong to another user");
        }

        ask.setAnswer(null);

        ask = askRepository.save(ask);

        return AskMapper.askToResponseAskDto(ask);
    }
}
