package com.springcloud.demo.asksmicroservice.exceptions;

import lombok.Getter;

@Getter
public class InheritedException extends  RuntimeException{

    private final int status;

    public InheritedException(int status, String message) {
        super(message);
        this.status = status;
    }

}
