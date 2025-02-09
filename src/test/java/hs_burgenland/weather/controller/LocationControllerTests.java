package hs_burgenland.weather.controller;

import com.sun.jdi.InternalException;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LocationControllerTests {
    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private Location location;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        location = new Location();
        location.setId(1);
        location.setName("Vienna,Austria");
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        location.setElevation(171.0);
        location.setIcao("LOWW");
    }

    @Test
    void createLocation_Success() throws EntityAlreadyExistingException {
        when(locationService.createLocation("Vienna")).thenReturn(location);

        final ResponseEntity<?> response = locationController.createLocation("Vienna");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(location.getName(), ((Location)Objects.requireNonNull(response.getBody())).getName());
        assertEquals(location.getLatitude(), ((Location)Objects.requireNonNull(response.getBody())).getLatitude());
        assertEquals(location.getLongitude(), ((Location)Objects.requireNonNull(response.getBody())).getLongitude());
        assertEquals(location.getElevation(), ((Location)Objects.requireNonNull(response.getBody())).getElevation());
        assertEquals(location.getIcao(), ((Location)Objects.requireNonNull(response.getBody())).getIcao());
    }

    @Test
    void createLocation_AlreadyExists() throws EntityAlreadyExistingException {
        when(locationService.createLocation("Vienna"))
                .thenThrow(new EntityAlreadyExistingException("Location Vienna,Austria does already exist on this bank."));

        final ResponseEntity<?> response = locationController.createLocation("Vienna");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Location Vienna,Austria does already exist on this bank.", response.getBody());
    }

    @Test
    void createLocation_EmptyName() throws EntityAlreadyExistingException {
        when(locationService.createLocation("")).thenThrow(new InternalException("Error while processing location data."));

        final ResponseEntity<?> response = locationController.createLocation("");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error while processing location data.", response.getBody());
    }

    @Test
    void createLocation_ServerError() throws EntityAlreadyExistingException {
        when(locationService.createLocation(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = locationController.createLocation("Vienna");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getAllLocations_EmptyList() {
        when(locationService.getAllLocations()).thenReturn(List.of());

        final ResponseEntity<?> response = locationController.getAllLocations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) Objects.requireNonNull(response.getBody())).isEmpty());
    }

    @Test
    void getAllLocations_WithEntries() {
        final List<Location> locations = List.of(location, new Location());
        when(locationService.getAllLocations()).thenReturn(locations);

        final ResponseEntity<?> response = locationController.getAllLocations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(locations, response.getBody());
    }

    @Test
    void getAllLocations_ServerError() {
        when(locationService.getAllLocations()).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = locationController.getAllLocations();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getLocationById_FoundEntry() throws EntityNotFoundException {
        when(locationService.getLocationById(1)).thenReturn(location);

        final ResponseEntity<?> response = locationController.getLocationById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(location, response.getBody());
    }

    @Test
    void getLocationById_NotFound() throws EntityNotFoundException {
        when(locationService.getLocationById(1)).thenThrow(new EntityNotFoundException("Location with id 1 not found."));

        final ResponseEntity<?> response = locationController.getLocationById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getLocationById_ServerError() throws EntityNotFoundException {
        when(locationService.getLocationById(1)).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = locationController.getLocationById(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void deleteLocation_Success() throws EntityNotFoundException {
        doNothing().when(locationService).deleteLocation(1);

        final ResponseEntity<?> response = locationController.deleteLocation(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteLocation_NotFound() throws EntityNotFoundException {
        doThrow(new EntityNotFoundException("Location with id 1 not found.")).when(locationService).deleteLocation(1);

        final ResponseEntity<?> response = locationController.deleteLocation(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteLocation_ServerError() throws EntityNotFoundException {
        doThrow(new RuntimeException("Unexpected error")).when(locationService).deleteLocation(1);

        final ResponseEntity<?> response = locationController.deleteLocation(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }
}
