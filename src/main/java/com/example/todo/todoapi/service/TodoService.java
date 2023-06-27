package com.example.todo.todoapi.service;

import com.example.todo.auth.TokenUserInfo;
import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.response.TodoDetailResponseDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.entity.Todo;
import com.example.todo.todoapi.repository.TodoRepository;
import com.example.todo.userapi.entity.Role;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional //jpa작동
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    //할 일 목록 조회
    //요청에 따리 데이터 갱신, 삭제 등이 발생한 수
    //최신의 데이터 내용을 클라이언트에게 전달해서 렌더링 하기 위해
    //목록 리턴 메서드를 서비스에서 처리.
    public TodoListResponseDTO retrieve(String userId){

        //로그인 한 유저의 정보 데이터베이스에서 조회
        User user = getUser(userId);

        List<Todo> entityList = todoRepository.findAllByUser(user);
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

    private User getUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("회원 정보가 없다.")
        );
        return user;
    }

    //할 일 삭제
    public TodoListResponseDTO delete(final String todoId, String userId) {
        try {
            todoRepository.deleteById(todoId);
        } catch (Exception e) {
            log.error("id가 존재하지않아 삭제에 실패했습니다. -ID:{}, err{}",todoId,e.getMessage());
            throw new RuntimeException("id가 존재하지않아 삭제에 실패했습니다.");
        }
        return retrieve(userId);//삭제후 다시 리스트 불러(새로고침)
    }

    public TodoListResponseDTO insert(TodoCreateRequestDTO dto, final TokenUserInfo userInfo
    ) throws RuntimeException{

        User foundUser = getUser(userInfo.getUserId());
        //권한에 따른 글쓰기 제한 처리
        //일반회원이 일정을 5개 초과해서 작성하면 예외를 발생
        if(userInfo.getRole() == Role.COMMON && todoRepository.countByUser(foundUser)>=5){
            throw new IllegalStateException("일반회원은 더이상 일정을 작성할 수 없습니다.");
        }

        Todo todo = dto.toEntity(foundUser);
        todoRepository.save(todo);
        return retrieve(userInfo.getUserId());
    }


    public TodoListResponseDTO modify(TodoModifyRequestDTO dto, String userId) {
        //수정 전 todo 가져오기
        Todo todo = getTodo(dto.getId());
        
        //변경
        todo.setDone(dto.isDone());

        //변경한 것 저장
        Todo modified = todoRepository.save(todo);
        return retrieve(userId);
    }

    private Todo getTodo(String id) {//todo 불러오기
        Todo todoEntity = todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo 없음"));
        return todoEntity;
    }
}
