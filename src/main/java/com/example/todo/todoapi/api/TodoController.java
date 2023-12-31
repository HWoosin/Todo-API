package com.example.todo.todoapi.api;

import com.example.todo.auth.TokenUserInfo;
import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.response.TodoDetailResponseDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/todos")
@CrossOrigin
//@CrossOrigin(origins = "http://localhost:3000")
public class TodoController {

    private final TodoService todoService;
    
    //할 일 등록 요청
    @PostMapping
    public ResponseEntity<?> createTodo(
            @AuthenticationPrincipal TokenUserInfo userInfo,
            @Validated @RequestBody TodoCreateRequestDTO requestDTO, BindingResult result){

        if(result.hasErrors()){
            log.warn("DTO 검증 에러발생:{}", result.getFieldErrorCount());
            return ResponseEntity.badRequest().body(result.getFieldErrorCount());
        }

        if(requestDTO == null){
            return ResponseEntity.badRequest().body("할 일을 작성해주세요");
        }

        try {
            TodoListResponseDTO responseDTO = todoService.insert(requestDTO, userInfo);
            return ResponseEntity.ok(responseDTO);
        }
        catch (IllegalStateException e){
            log.warn(e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("에러");
        }

    }

    //할 일 삭제 요청
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(
            @AuthenticationPrincipal TokenUserInfo userInfo,
            @PathVariable("id") String todoId){
        //id를 가져오기위해 PathVariable
        log.info("/api/todos/{} DELETE request!", todoId);
        if(todoId == null || todoId.trim().equals("")){
            return ResponseEntity
                    .badRequest()
                    .body(TodoListResponseDTO.builder().error("ID를 전달해 주세요."));
        }

        try {
            TodoListResponseDTO responseDTO = todoService.delete(todoId, userInfo.getUserId());
            return ResponseEntity.ok(responseDTO);//.body(responseDTO)
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(TodoListResponseDTO.builder().error(e.getMessage()));
        }

    }
    
    //할 일 목록 요청
    @GetMapping
    public ResponseEntity<?> retrieveTodoList(
            //토큰에 인증된 사용자 정보를 불러올 수 있음.
            @AuthenticationPrincipal TokenUserInfo userInfo
            ){

        TodoListResponseDTO responseDTO = todoService.retrieve(userInfo.getUserId());
        return ResponseEntity.ok(responseDTO);

    }

    //할 일 수정 요청
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateTodo(
            @AuthenticationPrincipal TokenUserInfo userInfo,
            @Validated @RequestBody TodoModifyRequestDTO dto, BindingResult result){
        if(result.hasErrors()){
            return ResponseEntity.badRequest().body(result.getFieldError());
        }

        try {
            TodoListResponseDTO responseDTO = todoService.modify(dto, userInfo.getUserId());
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(TodoListResponseDTO.builder().error(e.getMessage()));
        }

    }
}
