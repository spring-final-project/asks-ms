package com.springcloud.demo.asksmicroservice.asks.controller;

import com.springcloud.demo.asksmicroservice.asks.dto.AnswerAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.CreateAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.FilterAskDTO;
import com.springcloud.demo.asksmicroservice.asks.dto.ResponseAskDTO;
import com.springcloud.demo.asksmicroservice.asks.service.AskService;
import com.springcloud.demo.asksmicroservice.dto.SimpleResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asks")
@RequiredArgsConstructor
public class AskController {

    private final AskService askService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseAskDTO create(@Valid @RequestBody CreateAskDTO createAksDTO, @RequestHeader("X-UserId") String idUserLogged) {
        return askService.create(createAksDTO, idUserLogged);
    }

    @GetMapping
    List<ResponseAskDTO> findAll(@Valid @ModelAttribute FilterAskDTO filters) {
        return askService.findAll(filters);
    }

    @GetMapping("/{id}")
    ResponseAskDTO findById(@PathVariable @UUID String id) {
        return askService.findById(id);
    }

    @PostMapping("/{id}/answer")
    ResponseAskDTO answer(
            @PathVariable @UUID String id,
            @Valid @RequestBody AnswerAskDTO answerAskDTO,
            @RequestHeader("X-UserId") String idUserLogged
    ) {
        return askService.answer(id, answerAskDTO, idUserLogged);
    }

    @DeleteMapping("/{id}")
    SimpleResponseDTO delete(@PathVariable @UUID String id, @RequestHeader("X-UserId") String idUserLogged) {
        return askService.delete(id, idUserLogged);
    }

    @DeleteMapping("/{id}/answer")
    ResponseAskDTO deleteAnswer(@PathVariable @UUID String id, @RequestHeader("X-UserId") String idUserLogged) {
        return askService.deleteAnswer(id, idUserLogged);
    }
}
