package com.example.todo.userapi.service;

import com.example.todo.auth.TokenProvider;
import com.example.todo.auth.TokenUserInfo;
import com.example.todo.aws.S3Service;
import com.example.todo.exception.DuplicatedEmailException;
import com.example.todo.exception.NoRegisteredArgumentException;
import com.example.todo.userapi.dto.response.UserSignUpResponseDTO;
import com.example.todo.userapi.dto.request.LoginRequestDTO;
import com.example.todo.userapi.dto.request.UserSignUpRequestDTO;
import com.example.todo.userapi.dto.response.LoginResponseDTO;
import com.example.todo.userapi.entity.Role;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    private final S3Service s3Service;

//    @Value("${upload.path}")
//    private String uploadRootPath;

    public UserSignUpResponseDTO create(final UserSignUpRequestDTO dto, final String uploadedFilePath) throws RuntimeException{

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
        User user = dto.toEntity(uploadedFilePath);
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

    //프리미엄으로 등급 업
    public LoginResponseDTO promoteToPremium(TokenUserInfo userInfo)
            throws NoRegisteredArgumentException, IllegalStateException
    {

        User foundUser
                = userRepository.findById(userInfo.getUserId())
                .orElseThrow(
                        () -> new NoRegisteredArgumentException("회원조회에 실패!")
                );
        //일반 회원이 아니면 예외
        if(userInfo.getRole() != Role.COMMON){
            throw new IllegalStateException("일반 회원이 아니면 등급을 상승시킬 수 없다.");
        }
        //등급 변경
        foundUser.changeRole(Role.PREMIUM);
        User saved = userRepository.save(foundUser);

        //토큰을 재발급
        String token = tokenProvider.createToken(saved);
        return new LoginResponseDTO(saved, token);
    }

    /**
     * 업로드 된 파일을 서버에 저장하고 저장 경로를 리턴
     * @param originalFile - 업로드 된 파일의 정보
     * @return 실제로 저장된 이미지 경로
     */

    public String uploadProfileImage(MultipartFile originalFile) throws IOException {
        //루트 디렉토리가 존재하는 지 확인 후 존재하지 않으면 생성
//        File rootDir = new File(uploadRootPath);
//        if(!rootDir.exists()) rootDir.mkdir();
        //이제 AWS s3에 넣을것임

        //파일명을 유니크하게 변경
        String uniqueFileName = UUID.randomUUID()
                +"_"+originalFile.getOriginalFilename();

        //파일을 저장
//        File uploadFile = new File(uploadRootPath + "/" + uniqueFileName);
//        originalFile.transferTo(uploadFile);

        //파일을 S3 버킷에 저장 할것이다
        String uploadUrl
                = s3Service.uploadToS3Bucket(originalFile.getBytes(), uniqueFileName);
        return uploadUrl;

    }

    public String findProfilePath(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
//        return uploadRootPath + "/" + user.getProfileImg(); //그 유저의 이미지 파일경로의 파일 가져오기
        return user.getProfileImg();
    }
}





















