package com.example.todo.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NoRegisteredArgumentException extends RuntimeException{
    //자바에서 제공하지 않는 예외처리 만들기
    //비슷한 RuntimeException을 상속

    //기본 생성자 + 에러메세지를 받는 생성자


    public NoRegisteredArgumentException(String message) {
        super(message);
    }
}
