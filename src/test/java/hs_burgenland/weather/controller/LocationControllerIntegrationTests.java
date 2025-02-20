package hs_burgenland.weather.controller;

import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.services.LocationService;
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
class LocationControllerIntegrationTests {
    @Autowired
    private LocationService locationService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws EntityAlreadyExistingException {
        locationService.createLocation("Graz");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM location");
        jdbcTemplate.execute("ALTER TABLE location ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void createLocation_happyPath() throws Exception {
        mvc.perform(post("/locations")
                .contentType("application/json")
                .param("locationName", "Vienna"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 2, \"name\": \"Vienna,Austria\", \"latitude\": 48.20849, " +
                        "\"longitude\": 16.37208, \"elevation\": 171.0, \"icao\": \"LOWW\"}"));
    }

    @Test
    void createLocation_wrongInput() throws Exception {
        mvc.perform(post("/locations")
                .contentType("application/json")
                .param("locationName", "jashdgashujd"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error while processing location data. No results found."));
    }

    @Test
    void createLocation_existingLocation() throws Exception {
        mvc.perform(post("/locations")
                .contentType("application/json")
                .param("locationName", "Graz"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location Graz,Austria does already exist."));
    }

    @Test
    void getAllLocations_happyPath() throws Exception {
        mvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"latitude\":47.06667,\"longitude\":15.45," +
                        "\"elevation\":363.0,\"name\":\"Graz,Austria\",\"icao\":\"LOWG\"}]"));
    }

    @Test
    void getAllLocations_empty() throws Exception {
        locationService.deleteLocation(1);

        mvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getLocationById_happyPath() throws Exception {
        mvc.perform(get("/locations/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"latitude\":47.06667,\"longitude\":15.45," +
                        "\"elevation\":363.0,\"name\":\"Graz,Austria\",\"icao\":\"LOWG\"}"));
    }

    @Test
    void getLocationById_notExisting() throws Exception {
        mvc.perform(get("/locations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLocationById_wrongInputNumber() throws Exception {
        mvc.perform(get("/locations/0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location id must be greater than 0."));
    }

    @Test
    void getLocationById_wrongInput() throws Exception {
        mvc.perform(get("/locations/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocation_happyPath() throws Exception {
        mvc.perform(delete("/locations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLocation_notExisting() throws Exception {
        mvc.perform(delete("/locations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLocation_wrongInput() throws Exception {
        mvc.perform(delete("/locations/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocation_wrongInputNumber() throws Exception {
        mvc.perform(delete("/locations/0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location id must be greater than 0."));
    }
}