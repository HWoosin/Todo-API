package com.example.todo.userapi.api;

import com.example.todo.userapi.dto.UserSignUpResponseDTO;
import com.example.todo.userapi.dto.request.UserSignUpRequestDTO;
import com.example.todo.userapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    
    //이메일 중복 확인 요청 처리
    //GET: /api/auth/check?email=zzzz@xxx.com
    @GetMapping("/check")
    public ResponseEntity check(String email){
        if(email.trim().equals("")){
            return ResponseEntity.badRequest()
                    .body("이메일 없음.");
        }
        boolean resultFlag = userService.isDuplicate(email);
        log.info("{} 중복?? - {}", email, resultFlag);
        return ResponseEntity.ok().body(resultFlag);
    }

    //회원 가입 요청 처리
    //POST: /api/auth
    @PostMapping
    public ResponseEntity<?> signup(@Validated @RequestBody UserSignUpRequestDTO dto, BindingResult result){
        log.info("/api/auth POST - {}",dto);

        if(result.hasErrors()){
            log.warn(result.toString());
            return ResponseEntity.badRequest().body(result.getFieldError());
        }
        UserSignUpResponseDTO responseDTO = userService.create(dto);
        return null;
    }
}
