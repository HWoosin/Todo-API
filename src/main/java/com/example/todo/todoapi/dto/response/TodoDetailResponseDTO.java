package com.example.todo.todoapi.dto.response;

import com.example.todo.todoapi.entity.Todo;
import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoDetailResponseDTO {

    //리액트 투두아이템의 item안의 정보를 가져올것
    private String id;
    private String title;
    private boolean done;

    //엔티티를 DTO로 바꾸는 생성자

    public TodoDetailResponseDTO(Todo todo) {//엔티티를 줘야 DTO로 변환할거아녀~
        this.id = todo.getTodoId();
        this.title = todo.getTitle();
        this.done = todo.isDone(); //boolean은 get set이 아니여!
    }
}
