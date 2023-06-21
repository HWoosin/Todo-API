package com.example.todo.todoapi.api;

import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.response.TodoDetailResponseDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/todos")
public class TodoController {

    private final TodoService todoService;
    
    //할 일 등록 요청
    @PostMapping
    public ResponseEntity<?> createTodo(@Validated @RequestBody TodoCreateRequestDTO dto, BindingResult result){

        if(result.hasErrors()){
            log.warn("DTO 검증 에러발생:{}", result.getFieldErrorCount());
            return ResponseEntity.badRequest().body(result.getFieldErrorCount());
        }

        if(dto == null){
            return ResponseEntity.badRequest().body("할 일을 작성해주세요");
        }

        try {
            TodoListResponseDTO responseDTO = todoService.insert(dto);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("에러");
        }

    }

    //할 일 삭제 요청
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable("id") String todoId){
        //id를 가져오기위해 PathVariable
        log.info("/api/todos/{} DELETE request!", todoId);
        if(todoId == null || todoId.trim().equals("")){
            return ResponseEntity
                    .badRequest()
                    .body(TodoListResponseDTO.builder().error("ID를 전달해 주세요."));
        }

        try {
            TodoListResponseDTO responseDTO = todoService.delete(todoId);
            return ResponseEntity.ok(responseDTO);//.body(responseDTO)
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(TodoListResponseDTO.builder().error(e.getMessage()));
        }

    }
    
    //할 일 목록 요청
    @GetMapping
    public ResponseEntity<?> retrieveTodoList(){

        TodoListResponseDTO responseDTO = todoService.retrieve();
        return ResponseEntity.ok(responseDTO);

    }

    //할 일 수정 요청
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateTodo(@Validated @RequestBody TodoModifyRequestDTO dto, BindingResult result){
        if(result.hasErrors()){
            return ResponseEntity.badRequest().body(result.getFieldError());
        }

        try {
            todoService.modify(dto);
            return ResponseEntity.ok("수정완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(TodoListResponseDTO.builder().error(e.getMessage()));
        }

    }
}
