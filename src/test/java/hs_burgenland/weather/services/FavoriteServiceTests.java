package hs_burgenland.weather.services;

import hs_burgenland.weather.TestdataGenerator;
import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.repositories.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FavoriteServiceTests {
    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserService userService;

    @Mock
    LocationService locationService;

    @InjectMocks
    private FavoriteService favoriteService;

    private Favorite favorite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        favorite = TestdataGenerator.generateFavoriteTestdataWithId();
    }

    @Test
    void createFavorite_happyPath_userAndLocationAlreadyExisting() throws EntityNotFoundException, EntityAlreadyExistingException {
        final Favorite favorite = TestdataGenerator.generateFavoriteTestdataWithId();

        when(favoriteRepository.existsByNameAndUserId(favorite.getName(), favorite.getUser().getId())).thenReturn(false);
        when(userService.getUserById(favorite.getUser().getId())).thenReturn(favorite.getUser());
        when(locationService.getLocationByName(favorite.getLocation().getName())).thenReturn(Optional.of(favorite.getLocation()));
        when(favoriteRepository.existsByLocationIdAndUserId(favorite.getLocation().getId(), favorite.getUser().getId())).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);

        final Favorite createdFavorite = favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName());

        assertEquals(favorite, createdFavorite);
        verify(favoriteRepository, times(1)).existsByNameAndUserId(favorite.getName(), favorite.getUser().getId());
        verify(userService, times(1)).getUserById(favorite.getUser().getId());
        verify(locationService, times(1)).getLocationByName(favorite.getLocation().getName());
        verify(favoriteRepository, times(1)).existsByLocationIdAndUserId(favorite.getLocation().getId(), favorite.getUser().getId());
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void createFavorite_happyPath_locationNotExisting() throws EntityNotFoundException, EntityAlreadyExistingException {
        final Favorite favorite = TestdataGenerator.generateFavoriteTestdataWithId();

        when(favoriteRepository.existsByNameAndUserId(favorite.getName(), favorite.getUser().getId())).thenReturn(false);
        when(userService.getUserById(favorite.getUser().getId())).thenReturn(favorite.getUser());
        when(locationService.getLocationByName(favorite.getLocation().getName())).thenReturn(Optional.empty());
        when(locationService.createLocation(favorite.getLocation().getName())).thenReturn(favorite.getLocation());
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);

        final Favorite createdFavorite = favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName());

        assertEquals(favorite, createdFavorite);
        verify(favoriteRepository, times(1)).existsByNameAndUserId(favorite.getName(), favorite.getUser().getId());
        verify(userService, times(1)).getUserById(favorite.getUser().getId());
        verify(locationService, times(1)).getLocationByName(favorite.getLocation().getName());
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void createFavorite_locationNotExisting_exceptionAtCreatingLocation()
            throws EntityNotFoundException, EntityAlreadyExistingException {
        when(favoriteRepository.existsByNameAndUserId(favorite.getName(), favorite.getUser().getId())).thenReturn(false);
        when(userService.getUserById(favorite.getUser().getId())).thenReturn(favorite.getUser());
        when(locationService.getLocationByName(favorite.getLocation().getName())).thenReturn(Optional.empty());
        when(locationService.createLocation(favorite.getLocation().getName())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()));
        verify(favoriteRepository, times(1)).existsByNameAndUserId(favorite.getName(), favorite.getUser().getId());
        verify(userService, times(1)).getUserById(favorite.getUser().getId());
        verify(locationService, times(1)).getLocationByName(favorite.getLocation().getName());
        verify(locationService, times(1)).createLocation(any());
    }

    @Test
    void createFavorite_userNotExisting() throws EntityNotFoundException {
        when(favoriteRepository.existsByNameAndUserId(favorite.getName(), 99)).thenReturn(false);
        when(userService.getUserById(99))
                .thenThrow(new EntityNotFoundException("User with id 99 not found."));

        assertThrows(EntityNotFoundException.class, () -> favoriteService.createFavorite(favorite.getLocation().getName(), 99, favorite.getName()));
        verify(favoriteRepository, times(1)).existsByNameAndUserId(favorite.getName(), 99);
        verify(userService, times(1)).getUserById(99);
    }

    @Test
    void createFavorite_favoriteWithNameAlreadyExisting() {
        when(favoriteRepository.existsByNameAndUserId(favorite.getName(), favorite.getUser().getId())).thenReturn(true);

        assertThrows(EntityAlreadyExistingException.class, () -> favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()));
        verify(favoriteRepository, times(1)).existsByNameAndUserId(favorite.getName(), favorite.getUser().getId());
    }

    @Test
    void createFavorite_favoriteWithUserAndLocationAlreadyExisting() throws EntityNotFoundException {
        when(favoriteRepository.existsByNameAndUserId(favorite.getName(), favorite.getUser().getId())).thenReturn(false);
        when(userService.getUserById(favorite.getUser().getId())).thenReturn(favorite.getUser());
        when(locationService.getLocationByName(favorite.getLocation().getName())).thenReturn(Optional.of(favorite.getLocation()));
        when(favoriteRepository.existsByLocationIdAndUserId(favorite.getLocation().getId(), favorite.getUser().getId())).thenReturn(true);

        assertThrows(EntityAlreadyExistingException.class, () -> favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()));
        verify(favoriteRepository, times(1)).existsByNameAndUserId(favorite.getName(), favorite.getUser().getId());
        verify(userService, times(1)).getUserById(favorite.getUser().getId());
        verify(locationService, times(1)).getLocationByName(favorite.getLocation().getName());
        verify(favoriteRepository, times(1)).existsByLocationIdAndUserId(favorite.getLocation().getId(), favorite.getUser().getId());
    }

    @Test
    void createFavorite_databaseError() throws EntityNotFoundException, EntityAlreadyExistingException {
        when(favoriteRepository.existsByNameAndUserId(favorite.getName(), favorite.getUser().getId())).thenReturn(false);
        when(userService.getUserById(favorite.getUser().getId())).thenReturn(favorite.getUser());
        when(locationService.getLocationByName(favorite.getLocation().getName())).thenReturn(Optional.of(favorite.getLocation()));
        when(favoriteRepository.existsByLocationIdAndUserId(favorite.getLocation().getId(), favorite.getUser().getId())).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()));
        verify(favoriteRepository, times(1)).existsByNameAndUserId(favorite.getName(), favorite.getUser().getId());
        verify(userService, times(1)).getUserById(favorite.getUser().getId());
        verify(locationService, times(1)).getLocationByName(favorite.getLocation().getName());
        verify(favoriteRepository, times(1)).existsByLocationIdAndUserId(favorite.getLocation().getId(), favorite.getUser().getId());
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void getAllFavorites_success() {
        final List<Favorite> favorites = new ArrayList<>();
        favorites.add(new Favorite());
        favorites.add(new Favorite());

        when(favoriteRepository.findAll()).thenReturn(favorites);

        final List<Favorite> foundFavorites = favoriteService.getAllFavorites();

        assertEquals(2, foundFavorites.size());
        verify(favoriteRepository, times(1)).findAll();
    }

    @Test
    void getAllFavorites_emptyList() {
        when(favoriteRepository.findAll()).thenReturn(new ArrayList<>());

        final List<Favorite> foundFavorites = favoriteService.getAllFavorites();

        assertTrue(foundFavorites.isEmpty());
        verify(favoriteRepository, times(1)).findAll();
    }

    @Test
    void getAllFavorites_databaseError() {
        when(favoriteRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, favoriteService::getAllFavorites);
        verify(favoriteRepository, times(1)).findAll();
    }

    @Test
    void getFavoritesByUserId_success() throws EntityNotFoundException {
        when(userService.getUserById(1)).thenReturn(new User());
        when(favoriteRepository.findByUserId(1)).thenReturn(List.of(new Favorite(), new Favorite()));

        final List<Favorite> foundFavorites = favoriteService.getFavoritesByUserId(1);

        assertEquals(2, foundFavorites.size());
        verify(favoriteRepository, times(1)).findByUserId(1);
    }

    @Test
    void getFavoritesByUserId_emptyList() throws EntityNotFoundException {
        when(favoriteRepository.findByUserId(1)).thenReturn(new ArrayList<>());

        final List<Favorite> foundFavorites = favoriteService.getFavoritesByUserId(1);

        assertTrue(foundFavorites.isEmpty());
        verify(favoriteRepository, times(1)).findByUserId(1);
    }

    @Test
    void getFavoritesByUserId_wrongInputNumber() throws EntityNotFoundException {
        when(userService.getUserById(-1)).thenThrow(new EntityNotFoundException("User with id -1 not found."));

        assertThrows(EntityNotFoundException.class, () -> favoriteService.getFavoritesByUserId(-1));
        verify(userService, times(1)).getUserById(-1);
    }

    @Test
    void getFavoritesByUserId_databaseError() {
        when(favoriteRepository.findByUserId(1)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> favoriteService.getFavoritesByUserId(1));
        verify(favoriteRepository, times(1)).findByUserId(1);
    }

    @Test
    void getFavoriteById_success() throws EntityNotFoundException {
        when(favoriteRepository.findById(1)).thenReturn(Optional.of(favorite));

        final Favorite foundFavorite = favoriteService.getFavoriteById(1);

        assertEquals(1, foundFavorite.getId());
        assertEquals(favorite, foundFavorite);
        verify(favoriteRepository, times(1)).findById(1);
    }

    @Test
    void getFavoriteById_notFound() {
        when(favoriteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> favoriteService.getFavoriteById(99));
        verify(favoriteRepository, times(1)).findById(99);
    }

    @Test
    void getFavoriteById_databaseError() {
        when(favoriteRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> favoriteService.getFavoriteById(1));
        verify(favoriteRepository, times(1)).findById(1);
    }

    @Test
    void deleteFavorite_success() throws EntityNotFoundException {
        when(favoriteRepository.existsById(1)).thenReturn(true);

        favoriteService.deleteFavorite(1);

        verify(favoriteRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteFavorite_notFound() {
        when(favoriteRepository.existsById(99)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> favoriteService.deleteFavorite(99));
        verify(favoriteRepository, never()).deleteById(99);
    }

    @Test
    void deleteFavorite_databaseError() {
        when(favoriteRepository.existsById(1)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(favoriteRepository).deleteById(1);

        assertThrows(RuntimeException.class, () -> favoriteService.deleteFavorite(1));
        verify(favoriteRepository, times(1)).deleteById(1);
    }
}
