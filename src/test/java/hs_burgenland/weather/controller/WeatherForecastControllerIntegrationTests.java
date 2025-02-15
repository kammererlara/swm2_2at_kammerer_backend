package hs_burgenland.weather.controller;

import hs_burgenland.weather.TestdataGenerator;
import hs_burgenland.weather.entities.*;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.FavoriteService;
import hs_burgenland.weather.services.WeatherForecastService;
import org.apache.logging.log4j.util.InternalException;
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

    private Favorite favorite;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FavoriteService favoriteService() {
            return mock(FavoriteService.class, withSettings().strictness(Strictness.LENIENT));
        }

        @Bean
        public WeatherForecastService weatherForecastService() {
            return mock(WeatherForecastService.class, withSettings().strictness(Strictness.LENIENT));
        }
    }

    @BeforeEach
    void setUp() {
        reset(favoriteService);
        reset(weatherForecastService);
        favorite = TestdataGenerator.generateFavoriteTestdata();
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_Success() throws Exception {
        final List<WeatherRecord> weatherRecords = TestdataGenerator.generateWeatherRecords();

        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);
        when(weatherForecastService.getWeatherForecast(favorite)).thenReturn(weatherRecords);

        mvc.perform(get("/weather/1")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"time\":\"2025-02-13T00:00:00\",\"temperature\":-0.2,\"humidity\":90}," +
                        "{\"time\":\"2025-02-13T01:00:00\",\"temperature\":-0.1,\"humidity\":91}," +
                        "{\"time\":\"2025-02-13T02:00:00\",\"temperature\":0.0,\"humidity\":90}," +
                        "{\"time\":\"2025-02-13T03:00:00\",\"temperature\":0.0,\"humidity\":91}," +
                        "{\"time\":\"2025-02-13T04:00:00\",\"temperature\":0.0,\"humidity\":92}," +
                        "{\"time\":\"2025-02-13T05:00:00\",\"temperature\":0.1,\"humidity\":93}," +
                        "{\"time\":\"2025-02-13T06:00:00\",\"temperature\":0.3,\"humidity\":89}," +
                        "{\"time\":\"2025-02-13T07:00:00\",\"temperature\":0.4,\"humidity\":94}," +
                        "{\"time\":\"2025-02-13T08:00:00\",\"temperature\":0.6,\"humidity\":93}," +
                        "{\"time\":\"2025-02-13T09:00:00\",\"temperature\":0.7,\"humidity\":91}," +
                        "{\"time\":\"2025-02-13T10:00:00\",\"temperature\":1.0,\"humidity\":89}," +
                        "{\"time\":\"2025-02-13T11:00:00\",\"temperature\":1.6,\"humidity\":85}," +
                        "{\"time\":\"2025-02-13T12:00:00\",\"temperature\":1.4,\"humidity\":88}," +
                        "{\"time\":\"2025-02-13T13:00:00\",\"temperature\":1.9,\"humidity\":87}," +
                        "{\"time\":\"2025-02-13T14:00:00\",\"temperature\":2.3,\"humidity\":85}," +
                        "{\"time\":\"2025-02-13T15:00:00\",\"temperature\":1.9,\"humidity\":87}," +
                        "{\"time\":\"2025-02-13T16:00:00\",\"temperature\":1.9,\"humidity\":88}," +
                        "{\"time\":\"2025-02-13T17:00:00\",\"temperature\":1.9,\"humidity\":89}," +
                        "{\"time\":\"2025-02-13T18:00:00\",\"temperature\":1.7,\"humidity\":90}," +
                        "{\"time\":\"2025-02-13T19:00:00\",\"temperature\":1.9,\"humidity\":89}," +
                        "{\"time\":\"2025-02-13T20:00:00\",\"temperature\":1.9,\"humidity\":89}," +
                        "{\"time\":\"2025-02-13T21:00:00\",\"temperature\":2.2,\"humidity\":87}," +
                        "{\"time\":\"2025-02-13T22:00:00\",\"temperature\":2.4,\"humidity\":86}," +
                        "{\"time\":\"2025-02-13T23:00:00\",\"temperature\":2.9,\"humidity\":84}]"));
        verify(favoriteService, times(1)).getFavoriteById(1);
        verify(weatherForecastService, times(1)).getWeatherForecast(favorite);
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_favoriteNotExisting() throws Exception {
        when(favoriteService.getFavoriteById(1)).thenThrow(new EntityNotFoundException("Favorite with id 1 not found."));

        mvc.perform(get("/weather/1")
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
        verify(favoriteService, times(1)).getFavoriteById(1);
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_wrongIput() throws Exception {
        mvc.perform(get("/weather/-1")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite id must be greater than 0."));
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_openmeteoError() throws Exception {
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);
        when(weatherForecastService.getWeatherForecast(favorite))
                .thenThrow(new InternalException("Error while processing weather data."));

        mvc.perform(get("/weather/1")
                        .contentType("application/json"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error while processing weather data."));
        verify(favoriteService, times(1)).getFavoriteById(1);
        verify(weatherForecastService, times(1)).getWeatherForecast(favorite);
    }
}
