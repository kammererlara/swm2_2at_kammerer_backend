package hs_burgenland.weather.controller;

import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

//    DefaultUser: id = 1
    @PostMapping
    public ResponseEntity<?> createFavorite(@RequestBody final Favorite favorite) {
        if (favorite.getName() == null || favorite.getName().isEmpty()
                 || favorite.getLocation() == null) {
            return ResponseEntity.badRequest().body("Name and location must be provided.");
        }

        if (favorite.getUser() == null || favorite.getUser().getId() == 0) {
            final User user = new User();
            user.setId(1);
            favorite.setUser(user);
        }

        if (favorite.getUser().getId() <= 0) {
            return ResponseEntity.badRequest().body("User id must be greater than 0.");
        }

        try {
            return ResponseEntity.ok(favoriteService.createFavorite(favorite.getLocation().getName(), favorite.getUser().getId(), favorite.getName()));
        } catch (EntityAlreadyExistingException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllFavorites() {
        try {
            return ResponseEntity.ok(favoriteService.getAllFavorites());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getFavoritesByUserId(@PathVariable final int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body("User id must be greater than 0.");
        }

        try {
            return ResponseEntity.ok(favoriteService.getFavoritesByUserId(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFavoriteById(@PathVariable final int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body("Favorite id must be greater than 0.");
        }
        try {
            return ResponseEntity.ok(favoriteService.getFavoriteById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFavorite(@PathVariable final int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body("Favorite id must be greater than 0.");
        }
        try {
            favoriteService.deleteFavorite(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
