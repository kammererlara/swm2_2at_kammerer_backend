package hs_burgenland.weather.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hs_burgenland.weather.entities.Favorite;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.entities.WeatherRecord;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WeatherForecastService {
    @Autowired
    public LocationService locationService;

    @Autowired
    private ApiRequestService apiRequestService;

    final private ObjectMapper objectMapper;

    public WeatherForecastService() {
        this.objectMapper = new ObjectMapper();
    }

    public List<WeatherRecord> getWeatherForecast(final Favorite favorite) throws EntityNotFoundException {
        log.info("Getting weather forecast.");
        try {
            final Location location = locationService.getLocationById(favorite.getLocation().getId());
            return processWeatherData(location);
        } catch (JsonProcessingException e) {
            log.error("Error while processing weather data.", e);
            throw new InternalException("Error while processing weather data.", e);
        }
    }

    private List<WeatherRecord> processWeatherData(final Location location)
            throws JsonProcessingException {
        final String retrievedLocationData = apiRequestService.retrieveWeatherForecast(location);

        final JsonNode jsonNode = objectMapper.readTree(retrievedLocationData);

        if (jsonNode == null ||
                Math.round(jsonNode.path("latitude").asDouble()) != Math.round(location.getLatitude())
        || Math.round(jsonNode.path("longitude").asDouble()) != Math.round(location.getLongitude())) {
            throw new InternalException("Error while processing location data. Wrong location.");
        }

        final List<WeatherRecord> weatherRecords = new ArrayList<>();

        final JsonNode timeArray = jsonNode.path("hourly").path("time");
        final JsonNode temperatureArray = jsonNode.path("hourly").path("temperature_2m");
        final JsonNode humidityArray = jsonNode.path("hourly").path("relative_humidity_2m");

        for (int i = 0; i < timeArray.size(); i++) {
            weatherRecords
                    .add(new WeatherRecord(
                            LocalDateTime.parse(timeArray.get(i).asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            temperatureArray.get(i).asDouble(),
                            humidityArray.get(i).asInt()));
        }

        return weatherRecords;
    }
}
