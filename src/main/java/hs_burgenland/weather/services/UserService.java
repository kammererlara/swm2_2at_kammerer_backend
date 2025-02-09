package hs_burgenland.weather.services;

import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.repositories.UserRepository;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;

    public User createUser(final String firstname, final String lastname) throws EntityAlreadyExistingException {
        if (userRepository.getUserByFirstnameAndLastname(firstname, lastname).isPresent()) {
            throw new EntityAlreadyExistingException(String.format("User %s %s does already exist on this bank.", firstname, lastname));
        }

        final User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);

        return userRepository.save(user);
    }

    public User getUserById(final int id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found."));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(final int id) throws EntityNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with id " + id + " not found.");
        }
        userRepository.deleteById(id);
    }
}
