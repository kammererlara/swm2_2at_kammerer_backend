package hs_burgenland.weather.controller;

import hs_burgenland.weather.entities.*;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.FavoriteService;
import hs_burgenland.weather.services.UserService;
import hs_burgenland.weather.services.WeatherForecastService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherForecastControllerIntegrationTests {
    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private WeatherForecastService weatherForecastService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Favorite favorite;

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
    void getWeatherForecastAtFavoriteLocation_favoriteNotExisting() throws Exception {
        mvc.perform(get("/weather/99")
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_wrongIput() throws Exception {
        mvc.perform(get("/weather/-1")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite id must be greater than 0."));
    }
}
