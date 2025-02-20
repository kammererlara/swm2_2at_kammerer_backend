package hs_burgenland.weather.controller;

import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.FavoriteService;
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
class FavoriteControllerIntegrationTests {
    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws EntityAlreadyExistingException, EntityNotFoundException {
        userService.createUser("John", "Doe");
        favoriteService.createFavorite("Graz", 1, "Favorite");
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
    void createFavorite_happyPath() throws Exception {
        mvc.perform(post("/favorites")
                .contentType("application/json")
                .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\":1},\"name\":\"Home\"}"))
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
                        "{\"id\": 1, " +
                        "\"firstname\": \"John\", " +
                        "\"lastname\": \"Doe\"}, " +
                        "\"name\": \"Home\"}"));
    }

    @Test
    void createFavorite_wrongLocationInput() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"jhsdbfjkhabc\"},\"user\":{\"id\":1},\"name\": \"Home\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error while processing location data. No results found."));
    }

    @Test
    void createFavorite_wrongUserInput() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\":-1},\"name\":\"Home\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User id must be at least 0."));
    }

    @Test
    void createFavorite_notExistingUser() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\":99},\"name\":\"Home\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with id 99 not found."));
    }

    @Test
    void createFavorite_existingFavoriteName() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\": 1},\"name\":\"Favorite\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location with name Favorite is already a favorite location."));
    }

    @Test
    void createFavorite_existingFavoriteWithUserIdAndLocationCombination() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Graz\"},\"user\":{\"id\": 1},\"name\":\"Home\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location Graz,Austria does already exist."));
    }

    @Test
    void createFavorite_emptyInput() throws Exception {
        mvc.perform(post("/favorites")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name and location must be provided."));
    }

    @Test
    void getAllFavorites_happyPath() throws Exception {
        mvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("[{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\",\"lastname\":\"Doe\"}," +
                                "\"name\":\"Favorite\",\"location\":{\"id\":1,\"latitude\":47.06667," +
                                "\"longitude\":15.45,\"elevation\":363.0,\"name\":\"Graz,Austria\",\"icao\":\"LOWG\"}}]"));
    }

    @Test
    void getAllFavorites_emptyList() throws Exception {
        favoriteService.deleteFavorite(1);
        mvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getFavoritesByUserId_happyPath() throws Exception {
        mvc.perform(get("/favorites/user/1"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("[{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\",\"lastname\":\"Doe\"}," +
                        "\"name\":\"Favorite\",\"location\":{\"id\":1,\"latitude\":47.06667,\"longitude\":15.45," +
                        "\"elevation\":363.0,\"name\":\"Graz,Austria\",\"icao\":\"LOWG\"}}]"));
    }

    @Test
    void getFavoritesByUserId_notExisting() throws Exception {
        mvc.perform(get("/favorites/user/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFavoritesByUserId_wrongInputNumber() throws Exception {
        mvc.perform(get("/favorites/user/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User id must be at least 0."));
    }

    @Test
    void getFavoritesByUserId_wrongInput() throws Exception {
        mvc.perform(get("/favorites/user/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFavoriteById_happyPath() throws Exception {
        mvc.perform(get("/favorites/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\"," +
                        "\"lastname\":\"Doe\"},\"name\":\"Favorite\",\"location\":{\"id\":1,\"latitude\":47.06667," +
                        "\"longitude\":15.45,\"elevation\":363.0,\"name\":\"Graz,Austria\",\"icao\":\"LOWG\"}}"));
    }

    @Test
    void getFavoriteById_notExisting() throws Exception {
        mvc.perform(get("/favorites/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFavoriteById_wrongInputNumber() throws Exception {
        mvc.perform(get("/favorites/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite id must be greater than 0."));
    }

    @Test
    void getFavoriteById_wrongInput() throws Exception {
        mvc.perform(get("/favorites/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteFavorite_happyPath() throws Exception {
        mvc.perform(delete("/favorites/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteFavorite_notExisting() throws Exception {
        mvc.perform(delete("/favorites/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFavorite_wrongInputNumber() throws Exception {
        mvc.perform(delete("/favorites/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite id must be greater than 0."));
    }

    @Test
    void deleteFavorite_wrongInput() throws Exception {
        mvc.perform(delete("/favorites/abc"))
                .andExpect(status().isBadRequest());
    }
}