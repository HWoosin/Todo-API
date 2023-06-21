package com.example.todo.todoapi.service;

import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.response.TodoDetailResponseDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.entity.Todo;
import com.example.todo.todoapi.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional //jpa작동
public class TodoService {

    private final TodoRepository todoRepository;

    //할 일 목록 조회
    //요청에 따리 데이터 갱신, 삭제 등이 발생한 수
    //최신의 데이터 내용을 클라이언트에게 전달해서 렌더링 하기 위해
    //목록 리턴 메서드를 서비스에서 처리.
    public TodoListResponseDTO retrieve(){
        List<Todo> entityList = todoRepository.findAll();
//        List<TodoDetailResponseDTO> dtoList = new ArrayList<>();
//        for (Todo todo : entityList) {
//            TodoDetailResponseDTO dto = new TodoDetailResponseDTO(todo);
//            dtoList.add(dto);
//        }

        List<TodoDetailResponseDTO> dtoList = entityList.stream()
                .map(todo -> new TodoDetailResponseDTO(todo))
                .collect(Collectors.toList());

        return TodoListResponseDTO.builder()
                .todos(dtoList)//리스트에 담았으니까 리턴해야할꺼아냐
                .build();
    }

    //할 일 삭제
    public TodoListResponseDTO delete(final String todoId) {
        try {
            todoRepository.deleteById(todoId);
        } catch (Exception e) {
            log.error("id가 존재하지않아 삭제에 실패했습니다. -ID:{}, err{}",todoId,e.getMessage());
            throw new RuntimeException("id가 존재하지않아 삭제에 실패했습니다.");
        }
        return retrieve();//삭제후 다시 리스트 불러(새로고침)
    }

    public TodoListResponseDTO insert(TodoCreateRequestDTO dto) throws RuntimeException{
        todoRepository.save(dto.toEntity());
        return retrieve();
    }


    public TodoListResponseDTO modify(TodoModifyRequestDTO dto) {
        //수정 전 todo 가져오기
        Todo todo = getTodo(dto.getId());
        
        //변경
        todo.setDone(dto.isDone());

        //변경한 것 저장
        Todo modified = todoRepository.save(todo);
        return retrieve();
    }

    private Todo getTodo(String id) {//todo 불러오기
        Todo todoEntity = todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo 없음"));
        return todoEntity;
    }
}
