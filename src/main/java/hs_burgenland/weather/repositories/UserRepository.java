package hs_burgenland.weather.repositories;

import hs_burgenland.weather.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
