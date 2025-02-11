package hs_burgenland.weather.controller;

import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTests {
    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_happyPath() throws Exception {
        final User user = new User();
        user.setId(1);
        user.setFirstname("John");
        user.setLastname("Doe");
        when(userService.createUser("John", "Doe")).thenReturn(user);

        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{\"firstname\": \"John\", \"lastname\": \"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, \"firstname\": \"John\", \"lastname\": \"Doe\"}"));
        verify(userService, times(1)).createUser("John", "Doe");
    }

    @Test
    void createUser_wrongInput() throws Exception {
        when(userService.createUser(null, "Doe")).thenThrow(new ConstraintViolationException(
                "Not null constraint violation", new SQLException(), "firstname, lastname"));

        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{\"age\": 34, \"lastname\": \"Doe\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Not null constraint violation"));
        verify(userService, times(1)).createUser(null, "Doe");
    }

    @Test
    void createUser_existingUser() throws Exception {
        final User user = new User();
        user.setId(1);
        user.setFirstname("John");
        user.setLastname("Doe");
        when(userService.createUser("John", "Doe")).thenThrow(new EntityAlreadyExistingException(
                "User John Doe does already exist on this bank."));

        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{\"firstname\": \"John\", \"lastname\": \"Doe\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User John Doe does already exist on this bank."));
    }

    @Test
    void createUser_emptyInput() throws Exception {
        when(userService.createUser(null, null)).thenThrow(new ConstraintViolationException(
                "Not null constraint violation", new SQLException(), "firstname, lastname"));

        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Not null constraint violation"));
        verify(userService, times(1)).createUser(null, null);
    }

    @Test
    void listUsers_happyPath() throws Exception {
        final User user = new User();
        user.setId(1);
        user.setFirstname("John");
        user.setLastname("Doe");
        when(userService.getAllUsers()).thenReturn(List.of(user, user));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1, \"firstname\": \"John\", \"lastname\": \"Doe\"}," +
                        "{\"id\": 1, \"firstname\": \"John\", \"lastname\": \"Doe\"}]"));
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void readUser_happyPath() throws Exception {
        final User user = new User();
        user.setId(1);
        user.setFirstname("John");
        user.setLastname("Doe");
        when(userService.getUserById(1)).thenReturn(user);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, \"firstname\": \"John\", \"lastname\": \"Doe\"}"));
        verify(userService, times(1)).getUserById(1);
    }

    @Test
    void readUser_notExisting() throws Exception {
        when(userService.getUserById(99)).thenThrow(new EntityNotFoundException("User not found"));

        mvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).getUserById(99);
    }

    @Test
    void readUser_wrongInput() throws Exception {
        mvc.perform(get("/users/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_happyPath() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    void deleteUser_notExisting() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(userService).deleteUser(99);

        mvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).deleteUser(99);
    }

    @Test
    void deleteUser_wrongInput() throws Exception {
        mvc.perform(delete("/users/abc"))
                .andExpect(status().isBadRequest());
    }
}