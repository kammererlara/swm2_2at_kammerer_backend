package hs_burgenland.weather.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.repositories.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ApiRequestService apiRequestService;

    final private ObjectMapper objectMapper;

    public LocationService() {
        this.objectMapper = new ObjectMapper();
    }

    public Location createLocation(final String name) throws EntityAlreadyExistingException {
        try {
            final Location location = new Location();
            location.setName(name);
            storeLocationData(location);
            assertLocationDoesNotExist(location);
            storeAirportData(location);

            return locationRepository.save(location);
        } catch (JsonProcessingException e) {
            log.error("Error while processing location data.", e);
            throw new InternalException("Error while processing location data.", e);
        }
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(final int id) throws EntityNotFoundException {
        return locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + id + " not found."));
    }

    public void deleteLocation(final int id) throws EntityNotFoundException {
        if (!locationRepository.existsById(id)) {
            throw new EntityNotFoundException("Location with id " + id + " not found.");
        }
        locationRepository.deleteById(id);
    }

    public Optional<Location> getLocationByName(final String name) {
        return locationRepository.getLocationByName(name);
    }

    private void storeLocationData(final Location location)
            throws JsonProcessingException {
        final String retrievedLocationData = apiRequestService.retrieveLocationData(location);

        final JsonNode jsonNode = objectMapper.readTree(retrievedLocationData).path("results").get(0);

        if (jsonNode == null) {
            throw new InternalException("Error while processing location data. No results found.");
        }

        location.setName(jsonNode.path("name").asText() + "," + jsonNode.path("country").asText());
        location.setLatitude(jsonNode.path("latitude").asDouble());
        location.setLongitude(jsonNode.path("longitude").asDouble());
        location.setElevation(jsonNode.path("elevation").asDouble());
    }

    private void storeAirportData(final Location location)
            throws JsonProcessingException {
        final String retrievedAirportData = apiRequestService.retrieveAirportData(location);

        final JsonNode jsonNode = objectMapper.readTree(retrievedAirportData).get(0);
        if (jsonNode == null) {
            throw new InternalException("Error while processing location data. No results found.");
        }

        location.setIcao(jsonNode.path("station").path("icao").asText());
    }

    private void assertLocationDoesNotExist(final Location location) throws EntityAlreadyExistingException {
        if (locationRepository
                .getLocationByLatitudeAndLongitude(location.getLatitude(), location.getLongitude()).isPresent()) {
            throw new EntityAlreadyExistingException(
                    String.format("Location %s does already exist.", location.getName()));
        }
    }
}
