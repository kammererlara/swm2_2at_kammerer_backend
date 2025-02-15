package hs_burgenland.weather.controller;

import hs_burgenland.weather.TestdataGenerator;
import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.services.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FavoriteControllerDefaultUserIntegrationTests {
    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private MockMvc mvc;

    private Favorite favorite;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FavoriteService favoriteService() {
            return mock(FavoriteService.class, withSettings().strictness(Strictness.LENIENT));
        }
    }

    @BeforeEach
    void setUp() {
        reset(favoriteService);
        favorite = TestdataGenerator.generateFavoriteTestdata();
    }

    @Test
    void createFavorite_happyPath_defaultUser_UserIdNotGiven() throws Exception {
        favorite.getUser().setId(0);
        when(favoriteService.createFavorite("Vienna", favorite.getUser().getId(), favorite.getName()))
                .thenReturn(favorite);

        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"firstname\":\"Jane\"},\"name\":\"Home\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, " +
                        "\"location\": " +
                        "{\"id\": 1, " +
                        "\"name\": \"Vienna,Austria\", " +
                        "\"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, " +
                        "\"elevation\": 171.0, " +
                        "\"icao\": \"LOWW\"}, " +
                        "\"user\": " +
                        "{\"id\": 0, " +
                        "\"firstname\": \"John\", " +
                        "\"lastname\": \"Doe\"}, " +
                        "\"name\": \"Home\"}"));
        verify(favoriteService, times(1))
                .createFavorite("Vienna", favorite.getUser().getId(), favorite.getName());
    }

    @Test
    void createFavorite_defaultUser_UserNotGiven() throws Exception {
        favorite.getUser().setId(0);
        when(favoriteService.createFavorite("Vienna", favorite.getUser().getId(), favorite.getName()))
                .thenReturn(favorite);

        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"name\":\"Home\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, " +
                        "\"location\": " +
                        "{\"id\": 1, " +
                        "\"name\": \"Vienna,Austria\", " +
                        "\"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, " +
                        "\"elevation\": 171.0, " +
                        "\"icao\": \"LOWW\"}, " +
                        "\"user\": " +
                        "{\"id\": 0, " +
                        "\"firstname\": \"John\", " +
                        "\"lastname\": \"Doe\"}, " +
                        "\"name\": \"Home\"}"));
        verify(favoriteService, times(1))
                .createFavorite("Vienna", favorite.getUser().getId(), favorite.getName());
    }

    @Test
    void getFavoritesByUserId_happyPath_defaultUser() throws Exception {
        favorite.getUser().setId(0);
        when(favoriteService.getFavoritesByUserId(0)).thenReturn(List.of(favorite, favorite));

        mvc.perform(get("/favorites/user/0"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"user\":{\"id\":0,\"firstname\":\"John\"," +
                        "\"lastname\":\"Doe\"},\"name\":\"Home\",\"location\":{\"id\":1,\"latitude\":48.20849," +
                        "\"longitude\":16.37208,\"elevation\":171.0,\"name\":\"Vienna,Austria\",\"icao\":\"LOWW\"}}," +
                        "{\"id\":1,\"user\":{\"id\":0,\"firstname\":\"John\",\"lastname\":\"Doe\"},\"name\":\"Home\"," +
                        "\"location\":{\"id\":1,\"latitude\":48.20849,\"longitude\":16.37208,\"elevation\":171.0," +
                        "\"name\":\"Vienna,Austria\",\"icao\":\"LOWW\"}}]"));
        verify(favoriteService, times(1)).getFavoritesByUserId(0);
    }
}
