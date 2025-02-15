package hs_burgenland.weather.controller;

import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.services.FavoriteService;
import hs_burgenland.weather.services.WeatherForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherForecastController {
    @Autowired
    private WeatherForecastService weatherForecastService;

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/{favoriteId}")
    public ResponseEntity<?> getWeatherForecastAtFavoriteLocation(@PathVariable final int favoriteId) {
        if (favoriteId <= 0) {
            return ResponseEntity.badRequest().body("Favorite id must be greater than 0.");
        }

        try {
            final Favorite favorite = favoriteService.getFavoriteById(favoriteId);

            if (favorite.getLocation() == null || favorite.getLocation().getId() <= 0) {
                return ResponseEntity.badRequest().body("Location id must be greater than 0.");
            }

            return ResponseEntity.ok(weatherForecastService.getWeatherForecast(favorite));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
