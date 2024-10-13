package com.springcloud.demo.asksmicroservice.asks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.springcloud.demo.asksmicroservice.asks.dto.AnswerAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.CreateAskDTO;
import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import com.springcloud.demo.asksmicroservice.asks.repository.AskRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.springcloud.demo.asksmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.asksmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.asksmicroservice.messaging.MessagingProducer;
import org.hamcrest.Matchers;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AskTestIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AskRepository askRepository;

    @MockBean
    private RoomClientImpl roomClient;

    @MockBean
    private MessagingProducer messagingProducer;

    List<Ask> asks;

    @BeforeEach
    void setup() {
        askRepository.deleteAll();
        Ask ask1 = Ask
                .builder()
                .id(UUID.randomUUID().toString())
                .roomId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .question("First question")
                .createdAt(LocalDateTime.now())
                .build();

        Ask ask2 = Ask
                .builder()
                .id(UUID.randomUUID().toString())
                .roomId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .question("Second question")
                .answer("Answer of second question")
                .createdAt(LocalDateTime.now())
                .respondedAt(LocalDateTime.now())
                .build();

        asks = askRepository.saveAll(List.of(ask1, ask2));
    }

    @Nested
    class Create {

        CreateAskDTO createAskDTO;

        @BeforeEach
        void setup() {
            createAskDTO = new CreateAskDTO();
        }

        @Test
        void createAsk() throws Exception {
            createAskDTO.setQuestion("Fiest question");
            createAskDTO.setRoomId(UUID.randomUUID().toString());

            given(roomClient.findById(anyString())).willReturn(new RoomDTO());

            MvcResult result = mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(createAskDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.question").value(createAskDTO.getQuestion()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roomId").value(createAskDTO.getRoomId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isString())
                    .andReturn();

            String idAskCreated = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");

            Ask askCreated = askRepository.findById(idAskCreated).orElseThrow(() -> new AssertionFailure("Ask is not created in DB"));

            assertThat(askCreated.getQuestion()).isEqualTo(createAskDTO.getQuestion());
        }

        @Test
        void errorWhenMissingFields() throws Exception {
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(createAskDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(2));

            List<Ask> asksInDB = askRepository.findAll();
            assertThat(asksInDB.size()).isEqualTo(asks.size());
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAll() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/asks"))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(asks.size()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(asks.getFirst().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(asks.get(1).getId()));
        }

        @Test
        void findAllByUserId() throws Exception {
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .get("/api/asks")
                                    .queryParam("userId", asks.getFirst().getUserId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(asks.getFirst().getId()));
        }

        @Test
        void findAllByRoomId() throws Exception {
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .get("/api/asks")
                                    .queryParam("roomId", asks.get(1).getRoomId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(asks.get(1).getId()));
        }

        @Test
        void findAllWithPagination() throws Exception {
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .get("/api/asks")
                                    .queryParam("page", "2")
                                    .queryParam("limit", "1")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(asks.get(1).getId()));
        }
    }

    @Nested
    class FindById {

        @Test
        void findAskById() throws Exception {
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/asks/" + asks.getLast().getId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(asks.getLast().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.question").value(asks.getLast().getQuestion()));
        }

        @Test
        void errorWhenNotFoundAksById() throws Exception {
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/asks/" + UUID.randomUUID())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found ask with id")));

        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/asks/abcde")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

        }
    }

    @Nested
    class Answer {

        AnswerAskDTO answerAskDTO;

        @BeforeEach
        void setup() {
            answerAskDTO = new AnswerAskDTO();
        }

        @Test
        void answerAsk() throws Exception {
            answerAskDTO.setAnswer("New answer");

            RoomDTO roomDTO = RoomDTO.builder()
                    .id(asks.getFirst().getRoomId())
                    .ownerId(UUID.randomUUID().toString())
                    .build();
            given(roomClient.findById(anyString())).willReturn(roomDTO);

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks/" + asks.getFirst().getId() + "/answer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(answerAskDTO))
                                    .header("X-UserId", roomDTO.getOwnerId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(asks.getFirst().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.answer").value(answerAskDTO.getAnswer()));

            Ask askUpdated = askRepository.findById(asks.getFirst().getId()).orElseThrow(()-> new AssertionFailure("The ask should exist"));
            assertThat(askUpdated.getQuestion()).isEqualTo(asks.getFirst().getQuestion());
            assertThat(askUpdated.getAnswer()).isEqualTo(answerAskDTO.getAnswer());
        }

        @Test
        void errorWhenMissingAnswerField() throws Exception {
            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks/" + asks.getFirst().getId() + "/answer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(answerAskDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            Ask askUpdated = askRepository.findById(asks.getFirst().getId()).orElseThrow(()-> new AssertionFailure("The ask should exist"));
            assertThat(askUpdated.getQuestion()).isEqualTo(asks.getFirst().getQuestion());
            assertThat(askUpdated.getAnswer()).isNull();
        }

        @Test
        void errorWhenNotFoundAskById() throws Exception {
            answerAskDTO.setAnswer("New answer");

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks/" + UUID.randomUUID() + "/answer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(answerAskDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message",Matchers.containsString("Not found ask with id")));

            Ask askUpdated = askRepository.findById(asks.getFirst().getId()).orElseThrow(()-> new AssertionFailure("The ask should exist"));
            assertThat(askUpdated.getQuestion()).isEqualTo(asks.getFirst().getQuestion());
            assertThat(askUpdated.getAnswer()).isNull();
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            answerAskDTO.setAnswer("New answer");

            mockMvc
                    .perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks/abcde/answer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(answerAskDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            Ask askUpdated = askRepository.findById(asks.getFirst().getId()).orElseThrow(()-> new AssertionFailure("The ask should exist"));
            assertThat(askUpdated.getQuestion()).isEqualTo(asks.getFirst().getQuestion());
            assertThat(askUpdated.getAnswer()).isNull();
        }
    }

    @Nested
    class Delete {
        @Test
        void delete() throws Exception {
            RoomDTO roomDTO = RoomDTO.builder()
                    .id(asks.getFirst().getRoomId())
                    .ownerId(UUID.randomUUID().toString())
                    .build();
            given(roomClient.findById(anyString())).willReturn(roomDTO);

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + asks.getFirst().getId())
                            .header("X-UserId", roomDTO.getOwnerId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));

            askRepository.findById(asks.getFirst().getId()).ifPresent(ask -> {
                throw new AssertionFailure("Ask should have been deleted");
            });
        }

        @Test
        void errorWhenNotFoundAskById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + UUID.randomUUID())
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found ask with id")));

            askRepository.findById(asks.getFirst().getId()).orElseThrow(()-> new AssertionFailure("Ask should exist"));
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/abcde")
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            askRepository.findById(asks.getFirst().getId()).orElseThrow(()-> new AssertionFailure("Ask should exist"));
        }
    }

    @Nested
    class DeleteAnswer {
        @Test
        void deleteAnswer() throws Exception {
            RoomDTO roomDTO = RoomDTO.builder()
                    .id(asks.getFirst().getRoomId())
                    .ownerId(UUID.randomUUID().toString())
                    .build();
            given(roomClient.findById(anyString())).willReturn(roomDTO);

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + asks.getLast().getId() + "/answer")
                            .header("X-UserId", roomDTO.getOwnerId())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.question").value(asks.getLast().getQuestion()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.answer", Matchers.nullValue()));

            Ask askUpdated = askRepository.findById(asks.getLast().getId()).orElseThrow(()-> new AssertionFailure("Ask should exist"));
            assertThat(askUpdated.getQuestion()).isEqualTo(asks.getLast().getQuestion());
            assertThat(askUpdated.getAnswer()).isNull();
        }

        @Test
        void errorWhenNotFoundAskById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + UUID.randomUUID() + "/answer")
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found ask with id")));

            Ask askUpdated = askRepository.findById(asks.getLast().getId()).orElseThrow(()-> new AssertionFailure("Ask should exist"));
            assertThat(askUpdated.getQuestion()).isEqualTo(asks.getLast().getQuestion());
            assertThat(askUpdated.getAnswer()).isEqualTo(asks.getLast().getAnswer());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/abcde/answer")
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            Ask askUpdated = askRepository.findById(asks.getLast().getId()).orElseThrow(()-> new AssertionFailure("Ask should exist"));
            assertThat(askUpdated.getQuestion()).isEqualTo(asks.getLast().getQuestion());
            assertThat(askUpdated.getAnswer()).isEqualTo(asks.getLast().getAnswer());
        }
    }
}
