package hs_burgenland.weather.controller;

import com.sun.jdi.InternalException;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LocationControllerIntegrationTests {
    @Autowired
    private LocationService locationService;

    @Autowired
    private MockMvc mvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public LocationService locationService() {
            return mock(LocationService.class);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(locationService);
    }

    @Test
    void createLocation_happyPath() throws Exception {
        final Location location = new Location();
        location.setId(1);
        location.setName("Vienna,Austria");
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        location.setElevation(171.0);
        location.setIcao("LOWW");
        when(locationService.createLocation("Vienna")).thenReturn(location);

        mvc.perform(post("/locations")
                .contentType("application/json")
                .param("locationName", "Vienna"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, \"name\": \"Vienna,Austria\", \"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, \"elevation\": 171.0, \"icao\": \"LOWW\"}"));
        verify(locationService, times(1)).createLocation("Vienna");
    }

    @Test
    void createLocation_wrongInput() throws Exception {
        when(locationService.createLocation("123")).thenThrow(new InternalException("Error while processing location data."));

        mvc.perform(post("/locations")
                .contentType("application/json")
                .param("locationName", "123"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error while processing location data."));
        verify(locationService, times(1)).createLocation("123");
    }

    @Test
    void createLocation_existingLocation() throws Exception {
        when(locationService.createLocation("Vienna")).thenThrow(new EntityAlreadyExistingException(
                "Location Vienna,Austria does already exist on this bank."));

        mvc.perform(post("/locations")
                .contentType("application/json")
                .param("locationName", "Vienna"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location Vienna,Austria does already exist on this bank."));
    }

    @Test
    void getAllLocations_happyPath() throws Exception {
        final Location location = new Location();
        location.setId(1);
        location.setName("Vienna,Austria");
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        location.setElevation(171.0);
        location.setIcao("LOWW");

        when(locationService.getAllLocations()).thenReturn(List.of(location, location));

        mvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1, \"name\": \"Vienna,Austria\", \"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, \"elevation\": 171.0, \"icao\": \"LOWW\"}, " +
                        "{\"id\": 1, \"name\": \"Vienna,Austria\", \"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, \"elevation\": 171.0, \"icao\": \"LOWW\"}]"));
        verify(locationService, times(1)).getAllLocations();
    }

    @Test
    void getAllLocations_empty() throws Exception {
        when(locationService.getAllLocations()).thenReturn(List.of());

        mvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(locationService, times(1)).getAllLocations();
    }

    @Test
    void getLocationById_happyPath() throws Exception {
        final Location location = new Location();
        location.setId(1);
        location.setName("Vienna,Austria");
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        location.setElevation(171.0);
        location.setIcao("LOWW");
        when(locationService.getLocationById(1)).thenReturn(location);

        mvc.perform(get("/locations/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1, \"name\": \"Vienna,Austria\", \"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, \"elevation\": 171.0, \"icao\": \"LOWW\"}"));
        verify(locationService, times(1)).getLocationById(1);
    }

    @Test
    void getLocationById_notExisting() throws Exception {
        when(locationService.getLocationById(99)).thenThrow(new EntityNotFoundException("Location not found"));

        mvc.perform(get("/locations/99"))
                .andExpect(status().isNotFound());
        verify(locationService, times(1)).getLocationById(99);
    }

    @Test
    void getLocationById_wrongInputNumber() throws Exception {
        mvc.perform(get("/locations/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location id must be at least 0."));
    }

    @Test
    void getLocationById_wrongInput() throws Exception {
        mvc.perform(get("/locations/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocation_happyPath() throws Exception {
        doNothing().when(locationService).deleteLocation(1);

        mvc.perform(delete("/locations/1"))
                .andExpect(status().isNoContent());
        verify(locationService, times(1)).deleteLocation(1);
    }

    @Test
    void deleteLocation_notExisting() throws Exception {
        doThrow(new EntityNotFoundException("Location not found")).when(locationService).deleteLocation(99);

        mvc.perform(delete("/locations/99"))
                .andExpect(status().isNotFound());
        verify(locationService, times(1)).deleteLocation(99);
    }

    @Test
    void deleteLocation_wrongInput() throws Exception {
        mvc.perform(delete("/locations/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocation_wrongInputNumber() throws Exception {
        mvc.perform(delete("/locations/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location id must be at least 0."));
    }
}