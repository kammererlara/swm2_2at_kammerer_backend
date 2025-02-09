package hs_burgenland.weather.repositories;

import hs_burgenland.weather.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    Optional<Object> getLocationByLatitudeAndLongitude(double latitude, double longitude);
}