package hs_burgenland.weather.services;

import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.repositories.FavoriteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService userService;

    public Favorite createFavorite(final String locationName, final int userId, final String name)
            throws EntityNotFoundException, EntityAlreadyExistingException {

        final Favorite favorite = new Favorite();

        setFavoriteName(name, favorite, userId);
        setFavoriteUser(userId, favorite);
        setFavoriteLocation(locationName, userId, favorite);

        return favoriteRepository.save(favorite);
    }

    public List<Favorite> getAllFavorites() {
        return favoriteRepository.findAll();
    }

    public List<Favorite> getFavoritesByUserId(final int userId) throws EntityNotFoundException {
        userService.getUserById(userId);
        return favoriteRepository.findByUserId(userId);
    }

    public Favorite getFavoriteById(final int id) throws EntityNotFoundException {
        return favoriteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Favorite with id " + id + " not found."));
    }

    public void deleteFavorite(final int id) throws EntityNotFoundException {
        if (!favoriteRepository.existsById(id)) {
            throw new EntityNotFoundException("Favorite with id " + id + " not found.");
        }
        favoriteRepository.deleteById(id);
    }

    private void setFavoriteName(final String name, final Favorite favorite, final int userId) throws EntityAlreadyExistingException {
        if (favoriteRepository.existsByNameAndUserId(name, userId)) {
            throw new EntityAlreadyExistingException("Location with name " + name + " is already a favorite location.");
        }
        favorite.setName(name);
    }

    private void setFavoriteUser(final int userId, final Favorite favorite) throws EntityNotFoundException {
        final User user = userService.getUserById(userId);
        favorite.setUser(user);
    }

    private void setFavoriteLocation(final String locationName, final int userId, final Favorite favorite) throws EntityAlreadyExistingException {
        final Optional<Location> locationResult = locationService.getLocationByName(locationName);
        final Location location;
        if (locationResult.isPresent()) {
            location = locationResult.get();
            if (favoriteRepository.existsByLocationIdAndUserId(location.getId(), userId)) {
                throw new EntityAlreadyExistingException("Favorite with locationname " + location.getName() + " and userId " + userId + " already exists.");
            }
        } else {
                location = locationService.createLocation(locationName);
        }
        favorite.setLocation(location);
    }
}
