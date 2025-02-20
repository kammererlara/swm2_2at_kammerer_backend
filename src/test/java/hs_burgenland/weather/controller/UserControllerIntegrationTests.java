package hs_burgenland.weather.controller;

import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws EntityAlreadyExistingException {
        jdbcTemplate.execute("INSERT INTO users (id, firstname, lastname) VALUES (0, 'Jane', 'Doe')");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void createUser_happyPath() throws Exception {
        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{\"firstname\": \"Max\", \"lastname\": \"Muster\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, \"firstname\": \"Max\", \"lastname\": \"Muster\"}"));
    }

    @Test
    void createUser_wrongInput() throws Exception {
        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{\"age\": 34, \"lastname\": \"Doe\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User must have a firstname and a lastname."));
    }

    @Test
    void createUser_existingUser() throws Exception {
        userService.createUser("John", "Doe");

        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{\"firstname\": \"John\", \"lastname\": \"Doe\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User John Doe does already exist on this bank."));
    }

    @Test
    void createUser_emptyInput() throws Exception {
        mvc.perform(post("/users")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User must have a firstname and a lastname."));
    }

    @Test
    void getAllUsers_happyPath() throws Exception {
        userService.createUser("John", "Doe");
        userService.createUser("Max", "Muster");

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":0,\"firstname\":\"Jane\",\"lastname\":\"Doe\"}," +
                        "{\"id\":1,\"firstname\":\"John\",\"lastname\":\"Doe\"}," +
                        "{\"id\":2,\"firstname\":\"Max\",\"lastname\":\"Muster\"}]"));
    }

    @Test
    void getAllUsers_emptyList() throws Exception {
        jdbcTemplate.update("DELETE FROM users WHERE id = 0");
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getUserById_happyPath() throws Exception {
        userService.createUser("John", "Doe");

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, \"firstname\": \"John\", \"lastname\": \"Doe\"}"));
    }

    @Test
    void getUserById_notExisting() throws Exception {
        mvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_wrongInputNumber() throws Exception {
        mvc.perform(get("/users/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User id must be at least 0."));
    }

    @Test
    void getUserById_wrongInput() throws Exception {
        mvc.perform(get("/users/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_happyPath_defaultUser() throws Exception {
        mvc.perform(get("/users/0"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 0, \"firstname\": \"Jane\", \"lastname\": \"Doe\"}"));
    }

    @Test
    void deleteUser_happyPath() throws Exception {
        userService.createUser("John", "Doe");

        mvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notExisting() throws Exception {
        mvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_wrongInputNumber() throws Exception {
        mvc.perform(delete("/users/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User id must be greater than 0."));
    }

    @Test
    void deleteUser_wrongInput() throws Exception {
        mvc.perform(delete("/users/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_defaultUser() throws Exception {
        mvc.perform(delete("/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User id must be greater than 0."));
    }
}