package hs_burgenland.weather.controller;

import com.sun.jdi.InternalException;
import hs_burgenland.weather.TestdataGenerator;
import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.FavoriteService;
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

class FavoriteControllerTests {
    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private FavoriteController favoriteController;

    private Favorite favorite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        favorite = TestdataGenerator.generateFavoriteTestdata();
    }

    @Test
    void createFavorite_happyPath() throws EntityAlreadyExistingException, EntityNotFoundException {
        when(favoriteService.createFavorite(
                favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName())).thenReturn(favorite);

        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(favorite.getName(), ((Favorite)Objects.requireNonNull(response.getBody())).getName());
        assertEquals(favorite.getLocation(), ((Favorite)Objects.requireNonNull(response.getBody())).getLocation());
        assertEquals(favorite.getUser(), ((Favorite)Objects.requireNonNull(response.getBody())).getUser());
    }

    @Test
    void createFavorite_nameAlreadyExists() throws EntityAlreadyExistingException, EntityNotFoundException {
        when(favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new EntityAlreadyExistingException("Location with name Home is already a favorite location."));

        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Location with name Home is already a favorite location.", response.getBody());
    }

    @Test
    void createFavorite_favoriteToUserAndLocationAlreadyExists()
            throws EntityAlreadyExistingException, EntityNotFoundException {
        when(favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new EntityAlreadyExistingException("Favorite with locationname Vienna and userId 1 already exists."));

        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Favorite with locationname Vienna and userId 1 already exists.", response.getBody());
    }

    @Test
    void createFavorite_userNotExisting() throws EntityAlreadyExistingException, EntityNotFoundException {
        when(favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new EntityNotFoundException("User with id 1 not found."));

        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User with id 1 not found.", response.getBody());
    }

    @Test
    void createFavorite_wrongUserIdInput() {
        favorite.getUser().setId(-1);
        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User id must be at least 0.", response.getBody());
    }

    @Test
    void createFavorite_wrongLocationInput() throws EntityAlreadyExistingException, EntityNotFoundException {
        favorite.getLocation().setName("dfsjgsjf");

        when(favoriteService.createFavorite("dfsjgsjf", favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new InternalException("Error while processing location data."));

        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error while processing location data.", response.getBody());
    }

    @Test
    void createFavorite_emptyInput() throws EntityAlreadyExistingException, EntityNotFoundException {
        favorite.setLocation(null);
        favorite.setUser(null);
        favorite.setName(null);

        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Name and location must be provided.", response.getBody());
    }

    @Test
    void createFavorite_serverError() throws EntityAlreadyExistingException, EntityNotFoundException {
        when(favoriteService.createFavorite(anyString(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = favoriteController.createFavorite(favorite);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void createFavorite_happyPath_defaultUser() throws EntityAlreadyExistingException, EntityNotFoundException {
        favorite.getUser().setId(0);

        final Favorite inputFavorite = TestdataGenerator.generateFavoriteTestdata();
        inputFavorite.setUser(null);

        when(favoriteService.createFavorite(
                favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName())).thenReturn(favorite);

        final ResponseEntity<?> response = favoriteController.createFavorite(inputFavorite);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(favorite, Objects.requireNonNull(response.getBody()));
        verify(favoriteService, times(1))
                .createFavorite(favorite.getLocation().getName(), 0, favorite.getName());
    }

    @Test
    void getAllFavorites_withEntries() {
        final List<Favorite> favorites = List.of(favorite, new Favorite());
        when(favoriteService.getAllFavorites()).thenReturn(favorites);

        final ResponseEntity<?> response = favoriteController.getAllFavorites();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(favorites, response.getBody());
    }

    @Test
    void getAllFavorites_emptyList() {
        when(favoriteService.getAllFavorites()).thenReturn(List.of());

        final ResponseEntity<?> response = favoriteController.getAllFavorites();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) Objects.requireNonNull(response.getBody())).isEmpty());
    }

    @Test
    void getAllFavorites_serverError() {
        when(favoriteService.getAllFavorites()).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = favoriteController.getAllFavorites();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getFavoritesByUserId_withEntries() throws EntityNotFoundException {
        final List<Favorite> favorites = List.of(favorite, new Favorite());
        when(favoriteService.getFavoritesByUserId(1)).thenReturn(favorites);

        final ResponseEntity<?> response = favoriteController.getFavoritesByUserId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(favorites, response.getBody());
    }

    @Test
    void getFavoritesByUserId_emptyList() throws EntityNotFoundException {
        when(favoriteService.getFavoritesByUserId(1)).thenReturn(List.of());

        final ResponseEntity<?> response = favoriteController.getFavoritesByUserId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) Objects.requireNonNull(response.getBody())).isEmpty());
    }

    @Test
    void getFavoritesByUserId_wrongInput() {
        final ResponseEntity<?> response = favoriteController.getFavoritesByUserId(-1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User id must be at least 0.", response.getBody());
    }

    @Test
    void getFavoritesByUserId_userNotExisting() throws EntityNotFoundException {
        when(favoriteService.getFavoritesByUserId(99)).thenReturn(List.of());

        final ResponseEntity<?> response = favoriteController.getFavoritesByUserId(99);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) Objects.requireNonNull(response.getBody())).isEmpty());
    }

    @Test
    void getFavoritesByUserId_serverError() throws EntityNotFoundException {
        when(favoriteService.getFavoritesByUserId(1)).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = favoriteController.getFavoritesByUserId(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getFavoritesByUserId_defaultUser() throws EntityNotFoundException {
        final List<Favorite> favorites = List.of(favorite, new Favorite());
        when(favoriteService.getFavoritesByUserId(0)).thenReturn(favorites);

        final ResponseEntity<?> response = favoriteController.getFavoritesByUserId(0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(favorites, response.getBody());
    }

    @Test
    void getFavoriteById_foundEntry() throws EntityNotFoundException {
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);

        final ResponseEntity<?> response = favoriteController.getFavoriteById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(favorite, response.getBody());
    }

    @Test
    void getFavoriteById_notFound() throws EntityNotFoundException {
        when(favoriteService.getFavoriteById(1)).thenThrow(new EntityNotFoundException("Favorite with id 1 not found."));

        final ResponseEntity<?> response = favoriteController.getFavoriteById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getFavoriteById_wrongInput() {
        final ResponseEntity<?> response = favoriteController.getFavoriteById(-1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getFavoriteById_serverError() throws EntityNotFoundException {
        when(favoriteService.getFavoriteById(1)).thenThrow(new RuntimeException("Unexpected error"));

        final ResponseEntity<?> response = favoriteController.getFavoriteById(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void deleteFavorite_success() throws EntityNotFoundException {
        doNothing().when(favoriteService).deleteFavorite(1);

        final ResponseEntity<?> response = favoriteController.deleteFavorite(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteFavorite_notFound() throws EntityNotFoundException {
        doThrow(new EntityNotFoundException("Favorite with id 1 not found.")).when(favoriteService).deleteFavorite(1);

        final ResponseEntity<?> response = favoriteController.deleteFavorite(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteFavorite_wrongInput() {
        final ResponseEntity<?> response = favoriteController.deleteFavorite(-1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteFavorite_serverError() throws EntityNotFoundException {
        doThrow(new RuntimeException("Unexpected error")).when(favoriteService).deleteFavorite(1);

        final ResponseEntity<?> response = favoriteController.deleteFavorite(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }
}
