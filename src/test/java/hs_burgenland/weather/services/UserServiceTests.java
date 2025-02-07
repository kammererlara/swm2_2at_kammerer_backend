package hs_burgenland.weather.services;

import hs_burgenland.weather.entities.User;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.repositories.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTests {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_Success() throws EntityAlreadyExistingException {
        final String firstname = "John";
        final String lastname = "Doe";
        final User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);

        when(userRepository.getUserByFirstnameAndLastname(firstname, lastname)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        final User createdUser = userService.createUser(firstname, lastname);

        assertEquals(firstname, createdUser.getFirstname());
        assertEquals(lastname, createdUser.getLastname());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_AlreadyExists() {
        final String firstname = "Jane";
        final String lastname = "Doe";
        final User existingUser = new User();
        existingUser.setFirstname(firstname);
        existingUser.setLastname(lastname);
        when(userRepository.getUserByFirstnameAndLastname(firstname, lastname)).thenReturn(Optional.of(existingUser));

        assertThrows(EntityAlreadyExistingException.class, () -> userService.createUser(firstname, lastname));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_NullName() {
        final String firstname = null;
        final String lastname = null;

        when(userRepository.getUserByFirstnameAndLastname(firstname, lastname)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenThrow(new ConstraintViolationException("Not null constraint violation",
                        new SQLException(), "firstname, lastname"));

        assertThrows(ConstraintViolationException.class, () -> userService.createUser(firstname, lastname));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmptyName() throws EntityAlreadyExistingException {
        final String firstname = "";
        final String lastname = "";
        final User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);

        when(userRepository.getUserByFirstnameAndLastname(firstname, lastname)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        final User createdUser = userService.createUser(firstname, lastname);

        assertEquals(firstname, createdUser.getFirstname());
        assertEquals(lastname, createdUser.getLastname());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_DatabaseError() {
        final String firstname = "John";
        final String lastname = "Doe";
        final User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);

        when(userRepository.getUserByFirstnameAndLastname(firstname, lastname)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.createUser(firstname, lastname));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserById_Success() throws EntityNotFoundException {
        final int userId = 1;
        final User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        final User foundUser = userService.getUserById(userId);

        assertEquals(userId, foundUser.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_NotFound() {
        final int userId = 99;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_DatabaseError() {
        final int userId = 1;
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }


    @Test
    void getAllUsers_Success() {
        final List<User> users = new ArrayList<>();
        users.add(new User());
        users.add(new User());

        when(userRepository.findAll()).thenReturn(users);

        final List<User> foundUsers = userService.getAllUsers();

        assertEquals(2, foundUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        final List<User> foundUsers = userService.getAllUsers();

        assertTrue(foundUsers.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_DatabaseError() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, userService::getAllUsers);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_Success() throws EntityNotFoundException {
        final int userId = 1;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_NotFound() {
        final int userId = 99;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void deleteUser_DatabaseError() {
        final int userId = 1;
        when(userRepository.existsById(userId)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(userRepository).deleteById(userId);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }
}
