package hs_burgenland.weather.controller;

import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody final User user) {
        if (user.getFirstname() == null || user.getLastname() == null) {
            return ResponseEntity.badRequest().body("User must have a firstname and a lastname.");
        }
        try {
            return ResponseEntity.ok(userService.createUser(user.getFirstname(), user.getLastname()));
        } catch (EntityAlreadyExistingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable final int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body("User id must be greater than 0.");
        }

        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable final int id) {
        if (id <= 1) {
            // I want to prevent my default user from being deleted
            return ResponseEntity.badRequest().body("User id must be greater than 1.");
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
