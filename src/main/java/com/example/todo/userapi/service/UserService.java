package com.example.todo.userapi.service;

import com.example.todo.auth.TokenProvider;
import com.example.todo.exception.DuplicatedEmailException;
import com.example.todo.exception.NoRegisteredArgumentException;
import com.example.todo.userapi.dto.response.UserSignUpResponseDTO;
import com.example.todo.userapi.dto.request.LoginRequestDTO;
import com.example.todo.userapi.dto.request.UserSignUpRequestDTO;
import com.example.todo.userapi.dto.response.LoginResponseDTO;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;

    public UserSignUpResponseDTO create(final UserSignUpRequestDTO dto) throws RuntimeException{

        String email = dto.getEmail();

        if(dto == null){
            throw new NoRegisteredArgumentException("가입 정보가 없습니다.");
        }
        if(isDuplicate(email)){
            log.warn("이메일 중복 - {}", email);
            throw new DuplicatedEmailException("중복된 이메일 입니다.");
        }

        //패스워드 인코딩
        String encoded = encoder.encode(dto.getPassword());
        dto.setPassword(encoded);

        //유저 엔터티로 변환
        User user = dto.toEntity();
        User saved = userRepository.save(user);

        log.info("회원 가입 정상 수행됨! - saved user - {}", saved);

        return new UserSignUpResponseDTO(saved);
    }

    public boolean isDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    //회원 인증
    public LoginResponseDTO authenticate(final LoginRequestDTO dto){
        //이메일을 통해 회원정보를 조회.
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("가입된 회원이 아닙니다!"));

        //패스워드 검증
        String rawPassword = dto.getPassword(); //입력 비번
        String encodedPassword = user.getPassword(); //DB에 저장된 비번

        if(!encoder.matches(rawPassword, encodedPassword)){//일치하지않으면 에러
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        log.info("{}님 로그인 성공!",user.getUserName());

        //로그인 성공 후에 클라이언트에게 뭘 리턴할꺼임?? 토큰!
        // -> JWT를 클라이언트에게 발급해 줘야함.
        String token = tokenProvider.createToken(user);
        return new LoginResponseDTO(user, token);

    }
}





















