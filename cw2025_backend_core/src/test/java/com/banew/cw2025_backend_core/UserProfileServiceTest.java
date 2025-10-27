package com.banew.cw2025_backend_core;

import com.banew.cw2025_backend_common.dto.users.UserLoginForm;
import com.banew.cw2025_backend_common.dto.users.UserProfileBasicDto;
import com.banew.cw2025_backend_common.dto.users.UserRegisterForm;
import com.banew.cw2025_backend_common.dto.users.UserTokenFormResult;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import com.banew.cw2025_backend_core.backend.repo.UserProfileRepository;
import com.banew.cw2025_backend_core.backend.services.implementations.JwtServiceImpl;
import com.banew.cw2025_backend_core.backend.services.implementations.UserProfileServiceImpl;
import com.banew.cw2025_backend_core.backend.services.interfaces.JwtService;
import com.banew.cw2025_backend_core.backend.utils.BasicMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {
    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Spy
    private BasicMapper basicMapper = BasicMapper.INSTANCE;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Spy
    private JwtService jwtService = new JwtServiceImpl();

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker(Locale.UK);
        setField(jwtService, "key", "superSecret123");
        setField(jwtService, "days_life_token", 7L);
        setField(jwtService, "applicationName", "TestApp");
    }

    // ========== REGISTER TESTS ==========

    @Test
    void register_validForm_successfulRegistration() {
        // Given
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userProfileRepository.save(any())).thenAnswer(invocation -> {
            UserProfile user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserRegisterForm form = new UserRegisterForm();
        form.setEmail(faker.internet().emailAddress());
        form.setUsername(faker.name().fullName());
        form.setPassword(faker.credentials().password(8, 20));

        // When
        UserTokenFormResult result = userProfileService.register(form);

        // Then
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertFalse(result.getMessage().isEmpty());
        assertEquals(201, result.getCode());
        assertEquals(form.getUsername(), result.getUserProfile().getUsername());
        assertEquals(form.getEmail(), result.getUserProfile().getEmail());

        // Verify interactions
        verify(userProfileRepository).findByEmail(form.getEmail());
        verify(userProfileRepository).save(argThat(user ->
                user.getRoles().contains("USER") &&
                        !user.getPassword().equals(form.getPassword()) // password should be encoded
        ));
    }

    @Test
    void register_existingEmail_throwsException() {
        // Given
        String existingEmail = "existing@test.com";
        when(userProfileRepository.findByEmail(existingEmail))
                .thenReturn(Optional.of(new UserProfile()));

        UserRegisterForm form = new UserRegisterForm();
        form.setEmail(existingEmail);
        form.setUsername("Test User");
        form.setPassword("password123");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userProfileService.register(form));

        assertTrue(exception.getMessage().contains(existingEmail));
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void register_passwordIsEncoded() {
        // Given
        String rawPassword = "myPassword123";
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userProfileRepository.save(any())).thenAnswer(invocation -> {
            UserProfile user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserRegisterForm form = new UserRegisterForm();
        form.setEmail(faker.internet().emailAddress());
        form.setUsername(faker.name().fullName());
        form.setPassword(rawPassword);

        // When
        userProfileService.register(form);

        // Then
        verify(userProfileRepository).save(argThat(user -> {
            assertNotEquals(rawPassword, user.getPassword());
            assertTrue(passwordEncoder.matches(rawPassword, user.getPassword()));
            return true;
        }));
    }

    // ========== LOGIN TESTS ==========

    @Test
    void login_validCredentials_successfulLogin() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = passwordEncoder.encode(password);

        UserProfile existingUser = new UserProfile();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setPassword(encodedPassword);
        existingUser.setUsername("Test User");
        existingUser.setRoles(List.of("USER"));

        when(userProfileRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        UserLoginForm form = new UserLoginForm();
        form.setEmail(email);
        form.setPassword(password);

        // When
        UserTokenFormResult result = userProfileService.login(form);

        // Then
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(200, result.getCode());
        assertEquals("Successful login!", result.getMessage());
        assertEquals(email, result.getUserProfile().getEmail());
        verify(userProfileRepository).findByEmail(email);
    }

    @Test
    void login_wrongEmail_throwsException() {
        // Given
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        UserLoginForm form = new UserLoginForm();
        form.setEmail("nonexistent@test.com");
        form.setPassword("password123");

        // When & Then
        assertThrows(MyBadRequestException.class,
                () -> userProfileService.login(form));
    }

    @Test
    void login_wrongPassword_throwsException() {
        // Given
        String email = "test@example.com";
        String correctPassword = "correctPassword123";
        String wrongPassword = "wrongPassword456";

        UserProfile existingUser = new UserProfile();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setPassword(passwordEncoder.encode(correctPassword));
        existingUser.setRoles(List.of("USER"));

        when(userProfileRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        UserLoginForm form = new UserLoginForm();
        form.setEmail(email);
        form.setPassword(wrongPassword);

        // When & Then
        assertThrows(MyBadRequestException.class,
                () -> userProfileService.login(form));
    }

    // ========== UPDATE USER TESTS ==========

    @Test
    void updateUser_validData_updatesSuccessfully() {
        // Given
        UserProfile existingUser = new UserProfile();
        existingUser.setId(1L);
        existingUser.setEmail("old@test.com");
        existingUser.setUsername("Old Name");
        existingUser.setPhotoSrc("old-photo.jpg");

        UserProfileBasicDto updateDto = new UserProfileBasicDto();
        updateDto.setEmail("new@test.com");
        updateDto.setUsername("New Name");

        when(userProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserProfileBasicDto result = userProfileService.updateUser(updateDto, existingUser);

        // Then
        assertEquals("new@test.com", result.getEmail());
        assertEquals("New Name", result.getUsername());
        verify(userProfileRepository).save(existingUser);
    }

    @Test
    void updateUser_nullFields_keepsOldValues() {
        // Given
        UserProfile existingUser = new UserProfile();
        existingUser.setId(1L);
        existingUser.setEmail("old@test.com");
        existingUser.setUsername("Old Name");
        existingUser.setPhotoSrc("old-photo.jpg");

        UserProfileBasicDto updateDto = new UserProfileBasicDto();
        updateDto.setUsername("New Name");
        // email and photoSrc are null

        when(userProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserProfileBasicDto result = userProfileService.updateUser(updateDto, existingUser);

        // Then
        assertEquals("old@test.com", result.getEmail()); // not changed
        assertEquals("New Name", result.getUsername()); // changed
        assertEquals("old-photo.jpg", result.getPhotoSrc()); // not changed
    }

    // ========== GET USER BY ID TESTS ==========

    @Test
    void getUserById_existingUser_returnsUser() {
        // Given
        Long userId = 1L;
        UserProfile user = new UserProfile();
        user.setId(userId);
        user.setEmail("test@example.com");

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<UserProfile> result = userProfileService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userProfileRepository).findById(userId);
    }

    @Test
    void getUserById_nonExistingUser_returnsEmpty() {
        // Given
        Long userId = 999L;
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<UserProfile> result = userProfileService.getUserById(userId);

        // Then
        assertFalse(result.isPresent());
    }
}