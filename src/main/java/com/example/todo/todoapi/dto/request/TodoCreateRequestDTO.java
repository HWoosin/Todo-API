package com.example.todo.todoapi.dto.request;

import com.example.todo.todoapi.entity.Todo;
import com.example.todo.userapi.entity.User;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoCreateRequestDTO {

    @NotBlank
    @Size(min = 2, max = 10 )
    private String title; //id는 자동생성, done은 일단 false이기 때문에 title만 받아!

    public Todo toEntity() {//오버로딩
        return Todo.builder()
                .title(this.title)
                .build();

    }
    //dto를 엔터티로 변환
    public Todo toEntity(User user) {
        return Todo.builder()
                .title(this.title)
                .user(user)
                .build();

    }
}
