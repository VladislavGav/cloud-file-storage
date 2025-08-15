package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.dto.UserRequestDTO;
import by.kev.cloudfilestorage.dto.UserResponseDTO;
import by.kev.cloudfilestorage.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody @Valid UserRequestDTO userRequestDTO) {

        UserResponseDTO userResponseDTO = authService.register(userRequestDTO);

        return ResponseEntity.created(URI.create("api/user/me")).body(userResponseDTO);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDTO> signIn(@RequestBody @Valid UserRequestDTO userRequestDTO, HttpSession session) {

        UserResponseDTO userResponseDTO = authService.login(userRequestDTO, session);

        return ResponseEntity.ok(userResponseDTO);
    }




}
