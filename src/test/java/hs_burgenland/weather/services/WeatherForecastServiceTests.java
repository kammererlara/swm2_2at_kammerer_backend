package hs_burgenland.weather.services;

import hs_burgenland.weather.TestdataGenerator;
import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.entities.WeatherRecord;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import org.apache.logging.log4j.util.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class WeatherForecastServiceTests {
    @Mock
    LocationService locationService;

    @Mock
    ApiRequestService apiRequestService;

    @InjectMocks
    WeatherForecastService weatherForecastService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWeatherForecast_Success() throws EntityNotFoundException {
        final Favorite favorite = TestdataGenerator.generateFavoriteTestdata();

        final Location location = TestdataGenerator.generateLocationTestdata();

        when(locationService.getLocationById(1)).thenReturn(location);
        when(apiRequestService.retrieveWeatherForecast(location))
                .thenReturn("{\"latitude\":48.2,\"longitude\":16.38,\"generationtime_ms\":0.0472068786621094," +
                        "\"utc_offset_seconds\":0,\"timezone\":\"GMT\",\"timezone_abbreviation\":\"GMT\"," +
                        "\"elevation\":196,\"hourly_units\":{\"time\":\"iso8601\",\"temperature_2m\":\"°C\"," +
                        "\"relative_humidity_2m\":\"%\"},\"hourly\":{\"time\":[\"2025-02-13T00:00\"," +
                        "\"2025-02-13T01:00\",\"2025-02-13T02:00\",\"2025-02-13T03:00\",\"2025-02-13T04:00\"," +
                        "\"2025-02-13T05:00\",\"2025-02-13T06:00\",\"2025-02-13T07:00\",\"2025-02-13T08:00\"," +
                        "\"2025-02-13T09:00\",\"2025-02-13T10:00\",\"2025-02-13T11:00\",\"2025-02-13T12:00\"," +
                        "\"2025-02-13T13:00\",\"2025-02-13T14:00\",\"2025-02-13T15:00\",\"2025-02-13T16:00\"," +
                        "\"2025-02-13T17:00\",\"2025-02-13T18:00\",\"2025-02-13T19:00\",\"2025-02-13T20:00\"," +
                        "\"2025-02-13T21:00\",\"2025-02-13T22:00\",\"2025-02-13T23:00\"],\"temperature_2m\":[-0.2," +
                        "-0.1,0,0,0,0.1,0.3,0.4,0.6,0.7,1,1.6,1.4,1.9,2.3,1.9,1.9,1.9,1.7,1.9,1.9,2.2,2.4,2.9]," +
                        "\"relative_humidity_2m\":[90,91,90,91,92,93,89,94,93,91,89,85,88,87,85,87,88,89,90,89,89,87,86,84]}}");

        final List<WeatherRecord> result = weatherForecastService.getWeatherForecast(favorite);

        assertEquals(LocalDateTime.of(LocalDate.of(2025, 2, 13), LocalTime.of(0, 0)), result.getFirst().getTime());
        assertEquals(-0.2, result.getFirst().getTemperature());
        assertEquals(90, result.getFirst().getHumidity());

        verify(locationService, times(1)).getLocationById(1);
        verify(apiRequestService, times(1)).retrieveWeatherForecast(location);
    }

    @Test
    void getWeatherForecast_locationIdNotExisting() throws EntityNotFoundException {
        final Favorite favorite = TestdataGenerator.generateFavoriteTestdata();

        when(locationService.getLocationById(1)).thenThrow(new EntityNotFoundException("Location with id 1 not found."));

        assertThrows(EntityNotFoundException.class, () -> weatherForecastService.getWeatherForecast(favorite));
        verify(locationService, times(1)).getLocationById(1);
    }

    @Test
    void getWeatherForecast_openmeteoError() throws EntityNotFoundException {
        final Favorite favorite = TestdataGenerator.generateFavoriteTestdata();

        final Location location = TestdataGenerator.generateLocationTestdata();

        when(locationService.getLocationById(1)).thenReturn(location);
        when(apiRequestService.retrieveWeatherForecast(location))
                .thenReturn("{\"errormessage\": \"Connection error\"}");

        assertThrows(InternalException.class, () -> weatherForecastService.getWeatherForecast(favorite));
        verify(locationService, times(1)).getLocationById(1);
        verify(apiRequestService, times(1)).retrieveWeatherForecast(location);
    }
}