package hs_burgenland.weather.repositories;

import hs_burgenland.weather.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserId(int userId);
    boolean existsByLocationIdAndUserId(int locationId, int userId);
    boolean existsByNameAndUserId(String name, int userId);
}
