package hs_burgenland.weather.repositories;

import hs_burgenland.weather.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Integer> {
}