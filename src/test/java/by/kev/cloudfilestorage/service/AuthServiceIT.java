package by.kev.cloudfilestorage.service;

import by.kev.cloudfilestorage.dto.UserRequestDTO;
import by.kev.cloudfilestorage.dto.UserResponseDTO;
import by.kev.cloudfilestorage.exception.UserExistException;
import by.kev.cloudfilestorage.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
public class AuthServiceIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("testDb")
                    .withUsername("testUser")
                    .withPassword("testPass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("Test register")
    public void testRegister() {
        UserRequestDTO userRequestDTO = new UserRequestDTO("testUsername", "testPassword");

        UserResponseDTO userResponseDTO = authService.register(userRequestDTO);

        assertEquals("testUsername", userResponseDTO.username());
        assertTrue(userRepository.findByUsername("testUsername").isPresent());
    }

    @Test
    @DisplayName("Test register with throws UserExistException")
    public void testRegister_ExistingUser_ThrowsUserExistException() {
        UserRequestDTO userRequestDTO = new UserRequestDTO("test_username", "test_password");

        authService.register(userRequestDTO);

        assertThrows(UserExistException.class, () -> authService.register(userRequestDTO));
    }

}
