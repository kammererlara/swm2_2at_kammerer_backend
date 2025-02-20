package hs_burgenland.weather.controller;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FavoriteControllerDefaultUserTests {
    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private FavoriteController favoriteController;

    private Favorite favorite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        favorite = TestdataGenerator.generateFavoriteTestdataWithId();
    }

    @Test
    void createFavorite_happyPath_defaultUser() throws EntityAlreadyExistingException, EntityNotFoundException {
        favorite.getUser().setId(0);

        final Favorite inputFavorite = TestdataGenerator.generateFavoriteTestdataWithId();
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
    void getFavoritesByUserId_defaultUser() throws EntityNotFoundException {
        final List<Favorite> favorites = List.of(favorite, new Favorite());
        when(favoriteService.getFavoritesByUserId(0)).thenReturn(favorites);

        final ResponseEntity<?> response = favoriteController.getFavoritesByUserId(0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(favorites, response.getBody());
    }
}
