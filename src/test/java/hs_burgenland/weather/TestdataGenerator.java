package hs_burgenland.weather;

import hs_burgenland.weather.entities.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestdataGenerator {
    public static User generateUserTestdataWithoutId() {
        final User user = new User();
        user.setFirstname("John");
        user.setLastname("Doe");

        return user;
    }

    public static User generateUserTestdataWithId() {
        final User user = generateUserTestdataWithoutId();
        user.setId(1);
        return user;
    }

    public static Location generateLocationTestdataWithoutId() {
        final Location location = new Location();
        location.setName("Vienna,Austria");
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        location.setElevation(171.0);
        location.setIcao("LOWW");

        return location;
    }

    public static Location generateLocationTestdataWithId() {
        final Location location = generateLocationTestdataWithoutId();
        location.setId(1);
        return location;
    }

    public static Favorite generateFavoriteTestdataWithoutId() {
        final Favorite favorite = new Favorite();
        favorite.setLocation(generateLocationTestdataWithId());
        favorite.setUser(generateUserTestdataWithId());
        favorite.setName("Home");

        return favorite;
    }

    public static Favorite generateFavoriteTestdataWithId() {
        final Favorite favorite = generateFavoriteTestdataWithoutId();
        favorite.setId(1);
        return favorite;
    }

    public static List<WeatherRecord> generateWeatherRecords() {
        final List<WeatherRecord> weatherRecords = new ArrayList<>();
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 0, 0), -0.2, 90));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 1, 0), -0.1, 91));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 2, 0), 0.0, 90));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 3, 0), 0.0, 91));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 4, 0), 0.0, 92));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 5, 0), 0.1, 93));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 6, 0), 0.3, 89));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 7, 0), 0.4, 94));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 8, 0), 0.6, 93));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 9, 0), 0.7, 91));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 10, 0), 1.0, 89));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 11, 0), 1.6, 85));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 12, 0), 1.4, 88));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 13, 0), 1.9, 87));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 14, 0), 2.3, 85));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 15, 0), 1.9, 87));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 16, 0), 1.9, 88));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 17, 0), 1.9, 89));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 18, 0), 1.7, 90));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 19, 0), 1.9, 89));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 20, 0), 1.9, 89));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 21, 0), 2.2, 87));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 22, 0), 2.4, 86));
        weatherRecords.add(new WeatherRecord(LocalDateTime.of(2025, 2, 13, 23, 0), 2.9, 84));

        return weatherRecords;
    }

    public static User generateDefaultUserData() {
        final User user = new User();
        user.setId(0);
        user.setFirstname("Jane");
        user.setLastname("Doe");

        return user;
    }
}
