package com.springcloud.demo.asksmicroservice.asks.service;

import com.springcloud.demo.asksmicroservice.asks.dto.AnswerAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.CreateAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.FilterAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.ResponseAskDTO;
import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import com.springcloud.demo.asksmicroservice.asks.repository.AskRepository;

import static org.assertj.core.api.Assertions.*;

import com.springcloud.demo.asksmicroservice.asks.repository.AskSpecification;
import com.springcloud.demo.asksmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.asksmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.asksmicroservice.client.users.UserClientImpl;
import com.springcloud.demo.asksmicroservice.client.users.UserDTO;
import com.springcloud.demo.asksmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.asksmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.asksmicroservice.messaging.MessagingProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AskServiceTest {

    @Mock
    private AskRepository askRepository;

    @Mock
    private AskSpecification askSpecification;

    @Mock
    private RoomClientImpl roomClient;

    @Mock
    private UserClientImpl userClient;

    @Mock
    private MessagingProducer messagingProducer;

    @InjectMocks
    private AskService askService;

    Ask mockedAsk;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(askService, "askCreatedTopic", "topic1");

        mockedAsk = Ask
                .builder()
                .id(UUID.randomUUID().toString())
                .roomId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .question("Mock question")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    class Create {

        CreateAskDTO createAskDTO;

        @BeforeEach
        void setup() {
            createAskDTO = new CreateAskDTO();
        }

        @Test
        void createAks() {
            createAskDTO.setQuestion("Mock question");
            createAskDTO.setRoomId(UUID.randomUUID().toString());

            given(roomClient.findById(anyString())).willReturn(new RoomDTO());
            given(userClient.findById(any())).willReturn(new UserDTO());
            given(askRepository.save(any(Ask.class))).willReturn(mockedAsk);
            willDoNothing().given(messagingProducer).sendMessage(anyString(), anyString());

            ResponseAskDTO response = askService.create(createAskDTO, mockedAsk.getUserId());

            verify(roomClient).findById(createAskDTO.getRoomId());
            verify(askRepository).save(argThat(arg ->
                    arg.getQuestion().equals(createAskDTO.getQuestion()) &&
                    arg.getRoomId().equals(createAskDTO.getRoomId()) &&
                    arg.getUserId().equals(mockedAsk.getUserId())
            ));
            assertThat(response.getId()).isEqualTo(mockedAsk.getId());
            assertThat(response.getQuestion()).isEqualTo(mockedAsk.getQuestion());
            assertThat(response.getRoomId()).isEqualTo(mockedAsk.getRoomId());
            assertThat(response.getUserId()).isEqualTo(mockedAsk.getUserId());
        }
    }

    @Nested
    class FindAll {

        FilterAskDTO filters;
        List<Ask> asksFound;

        @BeforeEach
        void setup() {
            filters = new FilterAskDTO();
            asksFound = List.of(mockedAsk);
        }

        @Test
        void findAll() {
            given(askRepository.findAll((Specification<Ask>) any(), any(Pageable.class))).willReturn(new PageImpl<>(asksFound));

            List<ResponseAskDTO> response = askService.findAll(filters);

            verify(askSpecification).withFilters(filters);
            verify(askRepository).findAll(
                    eq(askSpecification.withFilters(filters)),
                    eq(PageRequest.of(0, 20))
            );
            assertThat(response).hasSameSizeAs(asksFound);
        }

        @Test
        void findAllWithFilters() {
            String roomId = UUID.randomUUID().toString();
            filters.setLimit(10);
            filters.setRoomId(roomId);

            given(askRepository.findAll((Specification<Ask>) any(), any(Pageable.class))).willReturn(new PageImpl<>(asksFound));

            List<ResponseAskDTO> response = askService.findAll(filters);

            verify(askSpecification).withFilters(filters);
            verify(askRepository).findAll(
                    eq(askSpecification.withFilters(argThat(f ->
                            f.getRoomId().equals(roomId)
                    ))),
                    eq(PageRequest.of(0, 10))
            );
            assertThat(response).hasSameSizeAs(asksFound);
        }
    }

    @Nested
    class FindById {

        String idToFind;

        @BeforeEach
        void setup() {
            idToFind = mockedAsk.getId();
        }

        @Test
        void findById() {
            given(askRepository.findById(anyString())).willReturn(Optional.of(mockedAsk));

            ResponseAskDTO response = askService.findById(idToFind);

            verify(askRepository).findById(idToFind);
            assertThat(response.getId()).isEqualTo(mockedAsk.getId());
            assertThat(response.getQuestion()).isEqualTo(mockedAsk.getQuestion());
            assertThat(response.getRoomId()).isEqualTo(mockedAsk.getRoomId());
            assertThat(response.getUserId()).isEqualTo(mockedAsk.getUserId());
        }

        @Test
        void errorWhenNotFoundAskById() {
            given(askRepository.findById(anyString())).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> {
                        askService.findById(idToFind);
                    }
            );

            verify(askRepository).findById(idToFind);
            assertThat(e.getMessage()).contains("Not found ask with id");
        }
    }

    @Nested
    class Answer {
        String idToUpdate;
        AnswerAskDTO answerAskDTO;
        Ask askUpdated;

        @BeforeEach
        void setup() {
            idToUpdate = mockedAsk.getId();
            answerAskDTO = new AnswerAskDTO("Response ask");
            askUpdated = Ask
                    .builder()
                    .id(mockedAsk.getId())
                    .roomId(mockedAsk.getRoomId())
                    .userId(mockedAsk.getUserId())
                    .question(mockedAsk.getQuestion())
                    .createdAt(mockedAsk.getCreatedAt())
                    .answer("Response ask")
                    .build();
        }

        @Test
        void answerAsk() {
            RoomDTO roomDTO = RoomDTO.builder()
                    .id(mockedAsk.getRoomId())
                    .ownerId(UUID.randomUUID().toString())
                    .build();
            given(askRepository.findById(anyString())).willReturn(Optional.of(mockedAsk));
            given(askRepository.save(any(Ask.class))).willReturn(askUpdated);
            given(roomClient.findById(anyString())).willReturn(roomDTO);

            ResponseAskDTO response = askService.answer(idToUpdate, answerAskDTO, roomDTO.getOwnerId());

            verify(askRepository).findById(idToUpdate);
            verify(askRepository).save(argThat(dto -> dto.getId().equals(idToUpdate) && dto.getAnswer().equals(answerAskDTO.getAnswer())));
            assertThat(response.getId()).isEqualTo(idToUpdate);
            assertThat(response.getAnswer()).isEqualTo(mockedAsk.getAnswer());
        }

        @Test
        void errorWhenNotFoundAskById() {
            given(askRepository.findById(anyString())).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> {
                        askService.answer(idToUpdate, answerAskDTO, mockedAsk.getUserId());
                    }
            );

            verify(askRepository).findById(idToUpdate);
            verify(askRepository, never()).save(any(Ask.class));
            assertThat(e.getMessage()).contains("Not found ask with id");
        }
    }

    @Nested
    class Delete {
        String id;

        @BeforeEach
        void setup(){
            id = UUID.randomUUID().toString();
        }

        @Test
        void delete(){
            RoomDTO roomDTO = RoomDTO.builder()
                    .id(mockedAsk.getRoomId())
                    .ownerId(UUID.randomUUID().toString())
                    .build();

            given(roomClient.findById(anyString())).willReturn(roomDTO);
            given(askRepository.findById(anyString())).willReturn(Optional.of(mockedAsk));
            willDoNothing().given(askRepository).delete(any(Ask.class));

            SimpleResponseDTO response = askService.delete(id, roomDTO.getOwnerId());

            verify(askRepository).findById(id);
            verify(askRepository).delete(mockedAsk);
            assertThat(response.isOk()).isTrue();
        }

        @Test
        void errorWhenNotFoundAskById(){
            given(askRepository.findById(anyString())).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, ()->{
                askService.delete(id, mockedAsk.getUserId());
            });

            verify(askRepository).findById(id);
            verify(askRepository, never()).delete(any(Ask.class));
            assertThat(e.getMessage()).contains("Not found ask with id");
        }
    }

    @Nested
    class DeleteAnswer {
        String idToDeleteAnswer;
        Ask askUpdated;

        @BeforeEach
        void setup(){
            idToDeleteAnswer = mockedAsk.getId();
            mockedAsk.setAnswer("Some answer");

            askUpdated = Ask
                    .builder()
                    .id(mockedAsk.getId())
                    .roomId(mockedAsk.getRoomId())
                    .userId(mockedAsk.getUserId())
                    .question(mockedAsk.getQuestion())
                    .createdAt(mockedAsk.getCreatedAt())
                    .answer(null)
                    .build();
        }

        @Test
        void deleteAnswer(){
            RoomDTO roomDTO = RoomDTO.builder()
                    .id(mockedAsk.getRoomId())
                    .ownerId(UUID.randomUUID().toString())
                    .build();

            given(roomClient.findById(anyString())).willReturn(roomDTO);
            given(askRepository.findById(anyString())).willReturn(Optional.of(mockedAsk));
            given(askRepository.save(any(Ask.class))).willReturn(askUpdated);

            ResponseAskDTO response = askService.deleteAnswer(idToDeleteAnswer, roomDTO.getOwnerId());

            verify(askRepository).findById(idToDeleteAnswer);
            verify(askRepository).save(argThat( ask -> ask.getAnswer() == null));
            assertThat(response.getId()).isEqualTo(idToDeleteAnswer);
            assertThat(response.getAnswer()).isNull();
        }

        @Test
        void errorWhenNotFoundAskById(){
            given(askRepository.findById(anyString())).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, ()-> {
                askService.deleteAnswer(idToDeleteAnswer, mockedAsk.getUserId());
            });

            verify(askRepository).findById(idToDeleteAnswer);
            verify(askRepository, never()).save(any(Ask.class));
            assertThat(e.getMessage()).contains("Not found ask with id");
        }
    }
}