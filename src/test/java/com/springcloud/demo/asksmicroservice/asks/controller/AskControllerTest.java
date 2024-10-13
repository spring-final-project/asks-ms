package com.springcloud.demo.asksmicroservice.asks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.asksmicroservice.asks.dto.AnswerAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.CreateAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.FilterAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.ResponseAskDTO;
import com.springcloud.demo.asksmicroservice.asks.service.AskService;
import com.springcloud.demo.asksmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.asksmicroservice.monitoring.TracingExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@WebMvcTest
class AskControllerTest {

    @MockBean
    private AskService askService;

    @MockBean
    private TracingExceptions tracingExceptions;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class Create {

        CreateAskDTO createAskDTO;
        ResponseAskDTO expectedResponse;

        @BeforeEach
        void setup() {
            createAskDTO = new CreateAskDTO();
            expectedResponse = ResponseAskDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .question("first question")
                    .roomId(UUID.randomUUID().toString())
                    .userId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now().toString())
                    .build();
        }

        @Test
        void createAsk() throws Exception {
            given(askService.create(any(CreateAskDTO.class), anyString())).willReturn(expectedResponse);

            createAskDTO.setQuestion("first question");
            createAskDTO.setRoomId(UUID.randomUUID().toString());

            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(createAskDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedResponse.getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(expectedResponse.getUserId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roomId").value(expectedResponse.getRoomId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.question").value(expectedResponse.getQuestion()));

            verify(askService).create(eq(createAskDTO), anyString());
        }

        @Test
        void errorWhenMissingFields() throws Exception {
            given(askService.create(any(CreateAskDTO.class), anyString())).willReturn(expectedResponse);

            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/api/asks")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(createAskDTO))
                                    .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(2));

            verify(askService, never()).create(eq(createAskDTO), anyString());
        }
    }

    @Nested
    class FindAll {

        List<ResponseAskDTO> expectedResponse;

        @BeforeEach
        void setup() {
            expectedResponse = List.of(
                    ResponseAskDTO.builder()
                            .id(UUID.randomUUID().toString())
                            .question("first question")
                            .roomId(UUID.randomUUID().toString())
                            .userId(UUID.randomUUID().toString())
                            .createdAt(LocalDateTime.now().toString())
                            .build(),
                    ResponseAskDTO.builder()
                            .id(UUID.randomUUID().toString())
                            .question("second question")
                            .roomId(UUID.randomUUID().toString())
                            .userId(UUID.randomUUID().toString())
                            .createdAt(LocalDateTime.now().toString())
                            .build()
            );
        }

        @Test
        void findAll() throws Exception {
            given(askService.findAll(any(FilterAskDTO.class))).willReturn(expectedResponse);

            mockMvc.perform(MockMvcRequestBuilders.get("/api/asks"))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(expectedResponse.size()));

            verify(askService).findAll(argThat(filter ->
                    filter.getPage() == 1 &&
                            filter.getLimit() == 20 &&
                            filter.getUserId() == null &&
                            filter.getRoomId() == null
            ));
        }

        @Test
        void findAllWithFilters() throws Exception {
            String roomId = UUID.randomUUID().toString();
            String userId = UUID.randomUUID().toString();
            String page = "2";
            String limit = "10";

            given(askService.findAll(any(FilterAskDTO.class))).willReturn(expectedResponse);

            mockMvc.perform(
                    MockMvcRequestBuilders
                            .get("/api/asks")
                            .queryParam("roomId", roomId)
                            .queryParam("userId", userId)
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(expectedResponse.size()));

            verify(askService).findAll(argThat(filter ->
                    filter.getPage() == 2 &&
                            filter.getLimit() == 10 &&
                            filter.getUserId().equals(userId) &&
                            filter.getRoomId().equals(roomId)
            ));
        }
    }

    @Nested
    class FindById {
        ResponseAskDTO expectedResponse;
        String idToFind;

        @BeforeEach
        void setup(){
            idToFind = UUID.randomUUID().toString();
            expectedResponse = ResponseAskDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .question("first question")
                    .roomId(UUID.randomUUID().toString())
                    .userId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now().toString())
                    .build();
        }

        @Test
        void findById() throws Exception {
            given(askService.findById(anyString())).willReturn(expectedResponse);

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/asks/" + idToFind))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedResponse.getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.question").value(expectedResponse.getQuestion()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roomId").value(expectedResponse.getRoomId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(expectedResponse.getUserId()));

            verify(askService).findById(idToFind);
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            idToFind = null;
            given(askService.findById(anyString())).willReturn(expectedResponse);

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/asks/" + idToFind))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(askService, never()).findById(anyString());
        }
    }

    @Nested
    class Answer {
        String idToUpdate = UUID.randomUUID().toString();
        ResponseAskDTO expectedResponse;
        AnswerAskDTO answerAskDTO;

        @BeforeEach
        void setup(){
            expectedResponse = ResponseAskDTO.builder()
                    .id(idToUpdate)
                    .question("first question")
                    .answer("first answer")
                    .respondedAt(LocalDateTime.now().toString())
                    .roomId(UUID.randomUUID().toString())
                    .userId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now().toString())
                    .build();

            answerAskDTO = new AnswerAskDTO();
        }

        @Test
        void answerAsk() throws Exception {
            answerAskDTO.setAnswer("first answer");
            given(askService.answer(anyString(), any(AnswerAskDTO.class), anyString())).willReturn(expectedResponse);

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/api/asks/" + idToUpdate + "/answer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(answerAskDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedResponse.getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.answer").value(expectedResponse.getAnswer()));

            verify(askService).answer(eq(idToUpdate), argThat(args -> args.getAnswer().equals(answerAskDTO.getAnswer())), anyString());
        }

        @Test
        void errorWhenMissingAnswerField() throws Exception {

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/api/asks/" + idToUpdate + "/answer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(answerAskDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(askService, never()).answer(anyString(), any(AnswerAskDTO.class), anyString());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            idToUpdate = "abcde";

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/api/asks/" + idToUpdate + "/answer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(answerAskDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(askService, never()).answer(anyString(), any(AnswerAskDTO.class), anyString());
        }
    }

    @Nested
    class Delete {

        String idToDelete;

        @BeforeEach
        void setup(){
            idToDelete = UUID.randomUUID().toString();
        }

        @Test
        void delete() throws Exception {
            given(askService.delete(anyString(), anyString())).willReturn(new SimpleResponseDTO(true));

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + idToDelete)
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));

            verify(askService).delete(eq(idToDelete), anyString());
        }
        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            idToDelete = "abcde";

            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + idToDelete)
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(askService, never()).delete(anyString(), anyString());
        }
    }

    @Nested
    class DeleteAnswer {
        String idToDeleteAnswer;
        ResponseAskDTO expectedResponse;

        @BeforeEach
        void setup(){
            idToDeleteAnswer = UUID.randomUUID().toString();
            expectedResponse = ResponseAskDTO.builder()
                    .id(idToDeleteAnswer)
                    .question("first question")
                    .respondedAt(LocalDateTime.now().toString())
                    .roomId(UUID.randomUUID().toString())
                    .userId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now().toString())
                    .build();
        }

        @Test
        void deleteAnswer() throws Exception {
            given(askService.deleteAnswer(anyString(), anyString())).willReturn(expectedResponse);

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + idToDeleteAnswer + "/answer")
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expectedResponse.getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.question").value(expectedResponse.getQuestion()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.answer").value(expectedResponse.getAnswer()));

            verify(askService).deleteAnswer(eq(idToDeleteAnswer), anyString());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            idToDeleteAnswer = "abcde";

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/asks/" + idToDeleteAnswer + "/answer")
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(askService, never()).deleteAnswer(anyString(), anyString());
        }
    }
}