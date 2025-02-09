package hs_burgenland.weather.controller;

import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserControllerTests {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1);
        user.setFirstname("John");
        user.setLastname("Doe");
    }

    @Test
    void createUser_Success() throws EntityAlreadyExistingException {
        when(userService.createUser("John", "Doe")).thenReturn(user);

        final ResponseEntity<?> response = userController.createUser(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user.getFirstname(), ((User)Objects.requireNonNull(response.getBody())).getFirstname());
        assertEquals(user.getLastname(), ((User)Objects.requireNonNull(response.getBody())).getLastname());
    }

    @Test
    void createUser_AlreadyExists() throws EntityAlreadyExistingException {
        when(userService.createUser("John", "Doe"))
                .thenThrow(new EntityAlreadyExistingException("User John Doe does already exist on this bank."));

        final ResponseEntity<?> response = userController.createUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User John Doe does already exist on this bank.", response.getBody());
    }

    @Test
    void createUser_NullName() throws EntityAlreadyExistingException {
        user.setFirstname(null);
        user.setLastname(null);

        when(userService.createUser(null, null))
                .thenThrow(new ConstraintViolationException("Not null constraint violation",
                        new SQLException(), "firstname, lastname"));

        final ResponseEntity<?> response = userController.createUser(user);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Not null constraint violation", response.getBody());
    }

    @Test
    void createUser_ServerError() throws EntityAlreadyExistingException {
        when(userService.createUser(anyString(), anyString())).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = userController.createUser(user);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getAllUsers_EmptyList() {
        when(userService.getAllUsers()).thenReturn(List.of());

        final ResponseEntity<?> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    @Test
    void getAllUsers_WithEntries() {
        final List<User> users = List.of(user, new User());
        when(userService.getAllUsers()).thenReturn(users);

        final ResponseEntity<?> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    void getAllUsers_ServerError() {
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = userController.getAllUsers();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getUserById_FoundEntry() throws EntityNotFoundException {
        when(userService.getUserById(1)).thenReturn(user);

        final ResponseEntity<?> response = userController.getUserById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void getUserById_NotFound() throws EntityNotFoundException {
        when(userService.getUserById(1)).thenThrow(new EntityNotFoundException("User with id 1 not found."));

        final ResponseEntity<?> response = userController.getUserById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUserById_ServerError() throws EntityNotFoundException {
        when(userService.getUserById(1)).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = userController.getUserById(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void deleteUser_Success() throws EntityNotFoundException {
        doNothing().when(userService).deleteUser(1);

        final ResponseEntity<?> response = userController.deleteUser(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteUser_NotFound() throws EntityNotFoundException {
        doThrow(new EntityNotFoundException("User with id 1 not found.")).when(userService).deleteUser(1);

        final ResponseEntity<?> response = userController.deleteUser(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteUser_ServerError() throws EntityNotFoundException {
        doThrow(new RuntimeException("Unexpected error")).when(userService).deleteUser(1);

        final ResponseEntity<?> response = userController.deleteUser(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }
}
