package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.dto.UserRequestDTO;
import by.kev.cloudfilestorage.dto.UserResponseDTO;
import by.kev.cloudfilestorage.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Auth API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody @Valid UserRequestDTO userRequestDTO,
                                                  HttpSession session) {

        UserResponseDTO userResponseDTO = authService.register(userRequestDTO, session);

        return ResponseEntity.created(URI.create("api/user/me")).body(userResponseDTO);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDTO> signIn(@RequestBody @Valid UserRequestDTO userRequestDTO,
                                                  HttpSession session) {

        UserResponseDTO userResponseDTO = authService.login(userRequestDTO, session);

        return ResponseEntity.ok(userResponseDTO);
    }


}
