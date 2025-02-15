package hs_burgenland.weather.controller;

import hs_burgenland.weather.TestdataGenerator;
import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.entities.WeatherRecord;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.FavoriteService;
import hs_burgenland.weather.services.WeatherForecastService;
import org.apache.logging.log4j.util.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WeatherForecastControllerTests {
    @Mock
    WeatherForecastService weatherForecastService;

    @Mock
    FavoriteService favoriteService;

    @InjectMocks
    WeatherForecastController weatherForecastController;

    private Favorite favorite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        favorite = TestdataGenerator.generateFavoriteTestdata();
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_Success() throws EntityNotFoundException {
        final List<WeatherRecord> weatherRecords = new ArrayList<>();
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);
        when(weatherForecastService.getWeatherForecast(favorite)).thenReturn(weatherRecords);

        final ResponseEntity<?> response = weatherForecastController.getWeatherForecastAtFavoriteLocation(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(weatherRecords, response.getBody());
        verify(favoriteService, times(1)).getFavoriteById(1);
        verify(weatherForecastService, times(1)).getWeatherForecast(favorite);
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_notExistingFavorite() throws EntityNotFoundException {
        when(favoriteService.getFavoriteById(1))
                .thenThrow(new EntityNotFoundException("Favorite with id 1 not found."));

        final ResponseEntity<?> response = weatherForecastController.getWeatherForecastAtFavoriteLocation(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(favoriteService, times(1)).getFavoriteById(1);
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_WrongInput() {
        final ResponseEntity<?> response = weatherForecastController.getWeatherForecastAtFavoriteLocation(0);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Favorite id must be greater than 0.", response.getBody());
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_notExistingLocation() throws EntityNotFoundException {
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);
        when(weatherForecastService.getWeatherForecast(favorite))
                .thenThrow(new EntityNotFoundException("Location with id 1 not found."));

        final ResponseEntity<?> response = weatherForecastController.getWeatherForecastAtFavoriteLocation(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(favoriteService, times(1)).getFavoriteById(1);
        verify(weatherForecastService, times(1)).getWeatherForecast(favorite);
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_favoriteWithoutLocation() throws EntityNotFoundException {
        favorite.setLocation(null);
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);

        final ResponseEntity<?> response = weatherForecastController.getWeatherForecastAtFavoriteLocation(1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Location id must be greater than 0.", response.getBody());
        verify(favoriteService, times(1)).getFavoriteById(1);
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_favoriteWithNegativeLocationId() throws EntityNotFoundException {
        favorite.getLocation().setId(-1);
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);

        final ResponseEntity<?> response = weatherForecastController.getWeatherForecastAtFavoriteLocation(1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Location id must be greater than 0.", response.getBody());
        verify(favoriteService, times(1)).getFavoriteById(1);
    }

    @Test
    void getWeatherForecastAtFavoriteLocation_openmeteoError() throws EntityNotFoundException {
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);
        when(weatherForecastService.getWeatherForecast(favorite))
                .thenThrow(new InternalException("Error while processing weather data."));

        final ResponseEntity<?> response = weatherForecastController.getWeatherForecastAtFavoriteLocation(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error while processing weather data.", response.getBody());
        verify(favoriteService, times(1)).getFavoriteById(1);
        verify(weatherForecastService, times(1)).getWeatherForecast(favorite);
    }
}
