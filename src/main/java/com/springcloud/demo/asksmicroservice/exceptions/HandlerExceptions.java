package com.springcloud.demo.asksmicroservice.exceptions;

import com.springcloud.demo.asksmicroservice.exceptions.dto.ErrorResponseDTO;
import com.springcloud.demo.asksmicroservice.monitoring.TracingExceptions;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.List;

@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class HandlerExceptions {

    private final TracingExceptions tracingExceptions;

    @ExceptionHandler(InheritedException.class)
    public ResponseEntity<ErrorResponseDTO> handleSimpleException(InheritedException e) {
        tracingExceptions.addExceptionMetadata(e.getMessage());

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .message(e.getMessage())
                .status(e.getStatus())
                .build();

        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundExceptions(NotFoundException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDTO handleForbiddenExceptions(ForbiddenException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler({BadRequestException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleBadRequestException(Exception e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<String> errors = e.getFieldErrors().stream().map(err -> err.getField() + " " + err.getDefaultMessage()).toList();

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMethodValidationException(HandlerMethodValidationException e){
        List<String> errors = new ArrayList<>();

        if(e.getDetailMessageArguments() != null){
            for (Object obj:e.getDetailMessageArguments()){
                errors.add(obj.toString());
            }
        }

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        String message = e.getMessage().split(":")[0];

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .build();
    }

    /**
     * Handle errors when not exist id user logged header
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMissingRequestHeaderException(MissingRequestHeaderException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
    }
}
