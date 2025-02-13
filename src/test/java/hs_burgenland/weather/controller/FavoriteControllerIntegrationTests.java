package hs_burgenland.weather.controller;

import com.sun.jdi.InternalException;
import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FavoriteControllerIntegrationTests {
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

        final Location location = new Location();
        location.setId(1);
        location.setName("Vienna,Austria");
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        location.setElevation(171.0);
        location.setIcao("LOWW");

        final User user = new User();
        user.setId(1);
        user.setFirstname("John");
        user.setLastname("Doe");

        favorite = new Favorite();
        favorite.setId(1);
        favorite.setLocation(location);
        favorite.setUser(user);
        favorite.setName("Home");
    }

    @Test
    void createFavorite_happyPath() throws Exception {
        when(favoriteService.createFavorite("Vienna", favorite.getUser().getId(), favorite.getName()))
                .thenReturn(favorite);

        mvc.perform(post("/favorites")
                .contentType("application/json")
                .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\":1},\"name\":\"Home\"}"))
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
                        "{\"id\": 1, " +
                        "\"firstname\": \"John\", " +
                        "\"lastname\": \"Doe\"}, " +
                        "\"name\": \"Home\"}"));
        verify(favoriteService, times(1)).createFavorite("Vienna", favorite.getUser().getId(), favorite.getName());
    }

    @Test
    void createFavorite_wrongLocationInput() throws Exception {
        when(favoriteService.createFavorite("jhsdbfjkhabc", favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new InternalException("Error while processing location data. No results found."));

        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"jhsdbfjkhabc\"},\"user\":{\"id\":1},\"name\": \"Home\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error while processing location data. No results found."));
        verify(favoriteService, times(1)).createFavorite("jhsdbfjkhabc", favorite.getUser().getId(), favorite.getName());
    }

    @Test
    void createFavorite_wrongUserInput() throws Exception {
        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\":-1},\"name\":\"Home\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User id must be at least 0."));
    }

    @Test
    void createFavorite_notExistingUser() throws Exception {
        when(favoriteService.createFavorite("Vienna", 99, favorite.getName()))
                .thenThrow(new EntityNotFoundException("User with id 99 not found."));

        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\":99},\"name\":\"Home\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with id 99 not found."));
        verify(favoriteService, times(1)).createFavorite("Vienna", 99, favorite.getName());
    }

    @Test
    void createFavorite_existingFavoriteName() throws Exception {
        when(favoriteService.createFavorite("Vienna", favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new EntityAlreadyExistingException("Location with name Home is already a favorite location."));

        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\": 1},\"name\":\"Home\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location with name Home is already a favorite location."));
    }

    @Test
    void createFavorite_existingFavoriteWithUserIdAndLocationCombination() throws Exception {
        when(favoriteService.createFavorite("Vienna", favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new EntityAlreadyExistingException("Favorite with locationname Vienna and userId 1 already exists."));

        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\": 1},\"name\":\"Home\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite with locationname Vienna and userId 1 already exists."));
        verify(favoriteService, times(1)).createFavorite("Vienna", favorite.getUser().getId(), favorite.getName());
    }

    @Test
    void createFavorite_emptyInput() throws Exception {
        mvc.perform(post("/favorites")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name and location must be provided."));
    }

    @Test
    void createFavorite_ServerError() throws Exception {
        when(favoriteService.createFavorite("Vienna", favorite.getUser().getId(), favorite.getName()))
                .thenThrow(new RuntimeException("Server Error"));

        mvc.perform(post("/favorites")
                        .contentType("application/json")
                        .content("{\"location\":{\"name\":\"Vienna\"},\"user\":{\"id\":1},\"name\":\"Home\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server Error"));
        verify(favoriteService, times(1)).createFavorite("Vienna", favorite.getUser().getId(), favorite.getName());
    }

    @Test
    void createFavorite_happyPath_defaultUser_UserNotGiven() throws Exception {
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
    void getAllFavorites_happyPath() throws Exception {
        when(favoriteService.getAllFavorites()).thenReturn(List.of(favorite, favorite));

        mvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\"," +
                        "\"lastname\":\"Doe\"},\"name\":\"Home\",\"location\":{\"id\":1,\"latitude\":48.20849," +
                        "\"longitude\":16.37208,\"elevation\":171.0,\"name\":\"Vienna,Austria\",\"icao\":\"LOWW\"}}," +
                        "{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\",\"lastname\":\"Doe\"},\"name\":\"Home\"," +
                        "\"location\":{\"id\":1,\"latitude\":48.20849,\"longitude\":16.37208,\"elevation\":171.0," +
                        "\"name\":\"Vienna,Austria\",\"icao\":\"LOWW\"}}]"));
        verify(favoriteService, times(1)).getAllFavorites();
    }

    @Test
    void getAllFavorites_emptyList() throws Exception {
        when(favoriteService.getAllFavorites()).thenReturn(List.of());

        mvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getFavoritesByUserId_happyPath() throws Exception {
        when(favoriteService.getFavoritesByUserId(1)).thenReturn(List.of(favorite, favorite));

        mvc.perform(get("/favorites/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\"," +
                        "\"lastname\":\"Doe\"},\"name\":\"Home\",\"location\":{\"id\":1,\"latitude\":48.20849," +
                        "\"longitude\":16.37208,\"elevation\":171.0,\"name\":\"Vienna,Austria\",\"icao\":\"LOWW\"}}," +
                        "{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\",\"lastname\":\"Doe\"},\"name\":\"Home\"," +
                        "\"location\":{\"id\":1,\"latitude\":48.20849,\"longitude\":16.37208,\"elevation\":171.0," +
                        "\"name\":\"Vienna,Austria\",\"icao\":\"LOWW\"}}]"));
        verify(favoriteService, times(1)).getFavoritesByUserId(1);
    }

    @Test
    void getFavoritesByUserId_notExisting() throws Exception {
        when(favoriteService.getFavoritesByUserId(99))
                .thenThrow(new EntityNotFoundException("User with id 99 not found."));

        mvc.perform(get("/favorites/user/99"))
                .andExpect(status().isNotFound());
        verify(favoriteService, times(1)).getFavoritesByUserId(99);
    }

    @Test
    void getFavoritesByUserId_wrongInputNumber() throws Exception {
        mvc.perform(get("/favorites/user/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User id must be at least 0."));
    }

    @Test
    void getFavoritesByUserId_wrongInput() throws Exception {
        mvc.perform(get("/favorites/user/abc"))
                .andExpect(status().isBadRequest());
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

    @Test
    void getFavoriteById_happyPath() throws Exception {
        when(favoriteService.getFavoriteById(1)).thenReturn(favorite);

        mvc.perform(get("/favorites/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"user\":{\"id\":1,\"firstname\":\"John\"," +
                        "\"lastname\":\"Doe\"},\"name\":\"Home\",\"location\":{\"id\":1,\"latitude\":48.20849," +
                        "\"longitude\":16.37208,\"elevation\":171.0,\"name\":\"Vienna,Austria\",\"icao\":\"LOWW\"}}"));
        verify(favoriteService, times(1)).getFavoriteById(1);
    }

    @Test
    void getFavoriteById_notExisting() throws Exception {
        when(favoriteService.getFavoriteById(99)).thenThrow(new EntityNotFoundException("Favorite not found"));

        mvc.perform(get("/favorites/99"))
                .andExpect(status().isNotFound());
        verify(favoriteService, times(1)).getFavoriteById(99);
    }

    @Test
    void getFavoriteById_wrongInputNumber() throws Exception {
        mvc.perform(get("/favorites/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite id must be greater than 0."));
    }

    @Test
    void getFavoriteById_wrongInput() throws Exception {
        mvc.perform(get("/favorites/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteFavorite_happyPath() throws Exception {
        doNothing().when(favoriteService).deleteFavorite(1);

        mvc.perform(delete("/favorites/1"))
                .andExpect(status().isNoContent());
        verify(favoriteService, times(1)).deleteFavorite(1);
    }

    @Test
    void deleteFavorite_notExisting() throws Exception {
        doThrow(new EntityNotFoundException("Favorite with id 99 not found.")).when(favoriteService).deleteFavorite(99);

        mvc.perform(delete("/favorites/99"))
                .andExpect(status().isNotFound());
        verify(favoriteService, times(1)).deleteFavorite(99);
    }

    @Test
    void deleteFavorite_wrongInputNumber() throws Exception {
        mvc.perform(delete("/favorites/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Favorite id must be greater than 0."));
    }

    @Test
    void deleteFavorite_wrongInput() throws Exception {
        mvc.perform(delete("/favorites/abc"))
                .andExpect(status().isBadRequest());
    }
}