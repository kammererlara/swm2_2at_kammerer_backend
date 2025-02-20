package hs_burgenland.weather.controller;

import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.repositories.FavoriteRepository;
import hs_burgenland.weather.repositories.LocationRepository;
import hs_burgenland.weather.repositories.UserRepository;
import hs_burgenland.weather.services.FavoriteService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class FavoriteControllerDefaultUserIntegrationTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() throws EntityAlreadyExistingException, EntityNotFoundException {
        jdbcTemplate.execute("INSERT INTO users (id, firstname, lastname) VALUES (0, 'Jane', 'Doe')");
        favoriteService.createFavorite("Graz", 0, "Home");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM favorite");
        jdbcTemplate.execute("ALTER TABLE favorite ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM location");
        jdbcTemplate.execute("ALTER TABLE location ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void createFavorite_happyPath_defaultUser_UserIdNotGiven() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"firstname\":\"Jane\"},\"name\":\"Favorite\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 2, " +
                        "\"location\": " +
                        "{\"id\": 2, " +
                        "\"name\": \"Vienna,Austria\", " +
                        "\"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, " +
                        "\"elevation\": 171.0, " +
                        "\"icao\": \"LOWW\"}, " +
                        "\"user\": " +
                        "{\"id\": 0, " +
                        "\"firstname\": \"Jane\", " +
                        "\"lastname\": \"Doe\"}, " +
                        "\"name\": \"Favorite\"}"));
    }

    @Test
    void createFavorite_defaultUser_UserNotGiven() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"name\":\"Favorite\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 2, " +
                        "\"location\": " +
                        "{\"id\": 2, " +
                        "\"name\": \"Vienna,Austria\", " +
                        "\"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, " +
                        "\"elevation\": 171.0, " +
                        "\"icao\": \"LOWW\"}, " +
                        "\"user\": " +
                        "{\"id\": 0, " +
                        "\"firstname\": \"Jane\", " +
                        "\"lastname\": \"Doe\"}, " +
                        "\"name\": \"Favorite\"}"));
    }

    @Test
    void getFavoritesByUserId_happyPath_defaultUser() throws Exception {
        mvc.perform(get("/favorites/user/0"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"user\":{\"id\":0,\"firstname\":\"Jane\"," +
                        "\"lastname\":\"Doe\"},\"name\":\"Home\",\"location\":{\"id\":1,\"latitude\":47.06667," +
                        "\"longitude\":15.45,\"elevation\":363.0,\"name\":\"Graz,Austria\",\"icao\":\"LOWG\"}}]"));
    }
}
