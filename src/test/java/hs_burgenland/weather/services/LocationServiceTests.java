package hs_burgenland.weather.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import hs_burgenland.weather.entities.Location;
import hs_burgenland.weather.exceptions.EntityAlreadyExistingException;
import hs_burgenland.weather.exceptions.EntityNotFoundException;
import hs_burgenland.weather.repositories.LocationRepository;
import org.apache.logging.log4j.util.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LocationServiceTests {
    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ApiRequestService apiRequestService;

    @InjectMocks
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createLocation_Success() throws EntityAlreadyExistingException {
        when(apiRequestService.retrieveLocationData(any())).thenReturn("{\"results\":[{\"id\":2761369," +
                "\"name\":\"Vienna\",\"latitude\":48.20849," +
                "\"longitude\":16.37208,\"elevation\":171.0,\"feature_code\":\"PPLC\"," +
                "\"country_code\":\"AT\",\"admin1_id\":2761367,\"admin2_id\":2761333," +
                "\"timezone\":\"Europe/Vienna\",\"population\":1691468," +
                "\"postcodes\":[\"1010\",\"1020\",\"1030\",\"1040\",\"1050\",\"1060\",\"1070\",\"1080\"," +
                "\"1090\",\"1100\",\"1110\",\"1120\",\"1130\",\"1140\",\"1150\",\"1160\",\"1170\",\"1180\"," +
                "\"1190\",\"1200\",\"1210\",\"1220\",\"1230\"],\"country_id\":2782113,\"country\":\"Austria\"," +
                "\"admin1\":\"Vienna\",\"admin2\":\"Vienna\"}],\"generationtime_ms\":1.2409687}");

        when(locationRepository.getLocationByLatitudeAndLongitude(48.208_49, 16.372_08))
                .thenReturn(Optional.empty());

        when(apiRequestService.retrieveAirportData(any())).thenReturn("[{\"coordinate_distance\":0.22067019115412875," +
                "\"kilometers\":18.27776052229729," +
                "\"miles\":11.357273847168342,\"nautical_miles\":9.869201145948859,\"station\":{" +
                "\"city\":\"Vienna\",\"country\":\"AT\",\"elevation_ft\":600,\"elevation_m\":183," +
                "\"gps\":\"LOWW\",\"iata\":\"VIE\",\"icao\":\"LOWW\",\"latitude\":48.110298,\"local\":null," +
                "\"longitude\":16.5697,\"name\":\"Vienna International Airport\",\"note\":null," +
                "\"reporting\":true,\"runways\":[{\"bearing1\":164.0,\"bearing2\":344.0,\"ident1\":\"16\"," +
                "\"ident2\":\"34\",\"length_ft\":11811,\"lights\":true,\"surface\":\"asphalt\"," +
                "\"width_ft\":148},{\"bearing1\":116.0,\"bearing2\":296.1,\"ident1\":\"11\",\"ident2\":\"29\"," +
                "\"length_ft\":11483,\"lights\":true,\"surface\":\"asphalt\",\"width_ft\":148}]," +
                "\"state\":\"9\",\"type\":\"large_airport\",\"website\":\"http://www.viennaairport.com/en/\"," +
                "\"wiki\":\"https://en.wikipedia.org/wiki/Vienna_International_Airport\"}}]\n");

        final Location expectedLocation = new Location();
        expectedLocation.setName("Vienna,Austria");
        expectedLocation.setLatitude(48.208_49);
        expectedLocation.setLongitude(16.372_08);
        expectedLocation.setElevation(171.0);
        expectedLocation.setIcao("LOWW");

        locationService.createLocation("Vienna");

        final ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());
        final Location capturedLocation = captor.getValue();

        assertEquals(expectedLocation.getName(), capturedLocation.getName());
        assertEquals(expectedLocation.getLatitude(), capturedLocation.getLatitude());
        assertEquals(expectedLocation.getLongitude(), capturedLocation.getLongitude());
        assertEquals(expectedLocation.getElevation(), capturedLocation.getElevation());
        assertEquals(expectedLocation.getIcao(), capturedLocation.getIcao());
    }

    @Test
    void createLocation_AlreadyExisting() {
        when(apiRequestService.retrieveLocationData(any())).thenReturn("{\"results\":[{\"id\":2761369," +
                "\"name\":\"Vienna\",\"latitude\":48.20849," +
                "\"longitude\":16.37208,\"elevation\":171.0,\"feature_code\":\"PPLC\"," +
                "\"country_code\":\"AT\",\"admin1_id\":2761367,\"admin2_id\":2761333," +
                "\"timezone\":\"Europe/Vienna\",\"population\":1691468," +
                "\"postcodes\":[\"1010\",\"1020\",\"1030\",\"1040\",\"1050\",\"1060\",\"1070\",\"1080\"," +
                "\"1090\",\"1100\",\"1110\",\"1120\",\"1130\",\"1140\",\"1150\",\"1160\",\"1170\",\"1180\"," +
                "\"1190\",\"1200\",\"1210\",\"1220\",\"1230\"],\"country_id\":2782113,\"country\":\"Austria\"," +
                "\"admin1\":\"Vienna\",\"admin2\":\"Vienna\"}],\"generationtime_ms\":1.2409687}");

        final Location location = new Location();
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        when(locationRepository.getLocationByLatitudeAndLongitude(48.208_49, 16.372_08))
                .thenReturn(Optional.of(location));

        assertThrows(EntityAlreadyExistingException.class, () -> locationService.createLocation("Vienna"));
    }

    @Test
    void createLocation_NoLocationFound() throws JsonProcessingException {
        when(apiRequestService.retrieveLocationData(any())).thenReturn("{\"generationtime_ms\":1.2620687}");

        assertThrows(InternalException.class, () -> locationService.createLocation("InvalidData"));
    }

    @Test
    void createLocation_geocodingApiNotWorking() {
        when(apiRequestService.retrieveLocationData(any()))
                .thenReturn("{\"error\": \"Service Unavailable\",\"status\": 503}");

        assertThrows(InternalException.class, () -> locationService.createLocation("TestCity"));
    }

    @Test
    void createLocation_avwxApiNotWorking() {
        when(apiRequestService.retrieveLocationData(any())).thenReturn("{\"results\":[{\"id\":2761369," +
                "\"name\":\"Vienna\",\"latitude\":48.20849," +
                "\"longitude\":16.37208,\"elevation\":171.0,\"feature_code\":\"PPLC\"," +
                "\"country_code\":\"AT\",\"admin1_id\":2761367,\"admin2_id\":2761333," +
                "\"timezone\":\"Europe/Vienna\",\"population\":1691468," +
                "\"postcodes\":[\"1010\",\"1020\",\"1030\",\"1040\",\"1050\",\"1060\",\"1070\",\"1080\"," +
                "\"1090\",\"1100\",\"1110\",\"1120\",\"1130\",\"1140\",\"1150\",\"1160\",\"1170\",\"1180\"," +
                "\"1190\",\"1200\",\"1210\",\"1220\",\"1230\"],\"country_id\":2782113,\"country\":\"Austria\"," +
                "\"admin1\":\"Vienna\",\"admin2\":\"Vienna\"}],\"generationtime_ms\":1.2409687}");

        when(locationRepository.getLocationByLatitudeAndLongitude(48.208_49, 16.372_08))
                .thenReturn(Optional.empty());

        when(apiRequestService.retrieveAirportData(any()))
                .thenReturn("{\"error\": \"Service Unavailable\",\"status\": 503}");

        assertThrows(InternalException.class, () -> locationService.createLocation("Vienna"));
    }

    @Test
    void createLocation_avwxApiWrongCoordinatesOrInvalidInput() {
        when(apiRequestService.retrieveLocationData(any()))
                .thenReturn("{\"error\": \"28.1,-381 is not a valid coordinate pair\"," +
                        "\"help\": \"Coordinate pair. Ex: \\\"12.34,-12.34\\\"\",\"param\": \"coord\"," +
                        "\"timestamp\": \"2025-02-09T15:16:22.297094Z\"}");

        when(locationRepository.getLocationByLatitudeAndLongitude(48.208_49, 16.372_08))
                .thenReturn(Optional.empty());

        when(apiRequestService.retrieveAirportData(any()))
                .thenReturn("{\"error\": \"Service Unavailable\",\"status\": 503}");

        assertThrows(InternalException.class, () -> locationService.createLocation("Vienna"));
    }

    @Test
    void createLocation_avwxWrongToken() {
        when(apiRequestService.retrieveLocationData(any())).thenReturn("{\"results\":[{\"name\":\"Vienna\"" +
                ",\"latitude\":48.20849,\"longitude\":16.37208,\"elevation\":171.0," +
                "\"country\":\"Austria\"}],\"generationtime_ms\":1.2409687}");

        when(locationRepository.getLocationByLatitudeAndLongitude(48.208_49, 16.372_08))
                .thenReturn(Optional.empty());

        when(apiRequestService.retrieveLocationData(any())).thenReturn("{\"results\":[{\"id\":2761369," +
                "\"name\":\"Vienna\",\"latitude\":48.20849," +
                "\"longitude\":16.37208,\"elevation\":171.0,\"feature_code\":\"PPLC\"," +
                "\"country_code\":\"AT\",\"admin1_id\":2761367,\"admin2_id\":2761333," +
                "\"timezone\":\"Europe/Vienna\",\"population\":1691468," +
                "\"postcodes\":[\"1010\",\"1020\",\"1030\",\"1040\",\"1050\",\"1060\",\"1070\",\"1080\"," +
                "\"1090\",\"1100\",\"1110\",\"1120\",\"1130\",\"1140\",\"1150\",\"1160\",\"1170\",\"1180\"," +
                "\"1190\",\"1200\",\"1210\",\"1220\",\"1230\"],\"country_id\":2782113,\"country\":\"Austria\"," +
                "\"admin1\":\"Vienna\",\"admin2\":\"Vienna\"}],\"generationtime_ms\":1.2409687}");

        when(locationRepository.getLocationByLatitudeAndLongitude(48.208_49, 16.372_08))
                .thenReturn(Optional.empty());

        when(apiRequestService.retrieveAirportData(any())).thenReturn("{\n" +
                "  \"meta\": {\n" +
                "    \"validation_error\": \"Your auth token is not allowed to access this resource. Token value could not be found. Here's an example response for testing purposes\"\n" +
                "  },\n" +
                "  \"sample\": [\n" +
                "    {\n" +
                "      \"coordinate_distance\": 0.35470923554481676,\n" +
                "      \"kilometers\": 34.79262287819728,\n" +
                "      \"miles\": 21.619133558889384,\n" +
                "      \"nautical_miles\": 18.786513433151878,\n" +
                "      \"station\": {\n" +
                "        \"city\": \"Melbourne\",\n" +
                "        \"country\": \"US\",\n" +
                "        \"elevation_ft\": 33,\n" +
                "        \"elevation_m\": 10,\n" +
                "        \"iata\": \"MLB\",\n" +
                "        \"icao\": \"KMLB\",\n" +
                "        \"latitude\": 28.102800369262695,\n" +
                "        \"longitude\": -80.64530181884766,\n" +
                "        \"name\": \"Melbourne International Airport\",\n" +
                "        \"note\": null,\n" +
                "        \"reporting\": true,\n" +
                "        \"runways\": [\n" +
                "          {\n" +
                "            \"ident1\": \"09R\",\n" +
                "            \"ident2\": \"27L\",\n" +
                "            \"length_ft\": 10181,\n" +
                "            \"width_ft\": 150\n" +
                "          },\n" +
                "          {\n" +
                "            \"ident1\": \"09L\",\n" +
                "            \"ident2\": \"27R\",\n" +
                "            \"length_ft\": 6000,\n" +
                "            \"width_ft\": 150\n" +
                "          },\n" +
                "          {\n" +
                "            \"ident1\": \"05\",\n" +
                "            \"ident2\": \"23\",\n" +
                "            \"length_ft\": 3001,\n" +
                "            \"width_ft\": 75\n" +
                "          }\n" +
                "        ],\n" +
                "        \"state\": \"FL\",\n" +
                "        \"type\": \"medium_airport\",\n" +
                "        \"website\": null,\n" +
                "        \"wiki\": \"http://en.wikipedia.org/wiki/Melbourne_International_Airport\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"coordinate_distance\": 0.5683025928495605,\n" +
                "      \"kilometers\": 58.922131095865765,\n" +
                "      \"miles\": 36.6125148482026,\n" +
                "      \"nautical_miles\": 31.815405559322766,\n" +
                "      \"station\": {\n" +
                "        \"city\": \"Cocoa Beach\",\n" +
                "        \"country\": \"US\",\n" +
                "        \"elevation_ft\": 10,\n" +
                "        \"elevation_m\": 3,\n" +
                "        \"iata\": null,\n" +
                "        \"icao\": \"KXMR\",\n" +
                "        \"latitude\": 28.4675998688,\n" +
                "        \"longitude\": -80.56659698490002,\n" +
                "        \"name\": \"Cape Canaveral AFS Skid Strip\",\n" +
                "        \"note\": null,\n" +
                "        \"reporting\": true,\n" +
                "        \"runways\": [\n" +
                "          {\n" +
                "            \"ident1\": \"13\",\n" +
                "            \"ident2\": \"31\",\n" +
                "            \"length_ft\": 10000,\n" +
                "            \"width_ft\": 200\n" +
                "          }\n" +
                "        ],\n" +
                "        \"state\": \"FL\",\n" +
                "        \"type\": \"medium_airport\",\n" +
                "        \"website\": null,\n" +
                "        \"wiki\": \"http://en.wikipedia.org/wiki/Cape_Canaveral_AFS_Skid_Strip\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        assertThrows(InternalException.class, () -> locationService.createLocation("Vienna"));
    }

    @Test
    void createLocation_DatabaseError() {
        when(apiRequestService.retrieveLocationData(any())).thenReturn("{\"results\":[{\"name\":\"Vienna\"" +
                ",\"latitude\":48.20849,\"longitude\":16.37208,\"elevation\":171.0," +
                "\"country\":\"Austria\"}],\"generationtime_ms\":1.2409687}");

        when(locationRepository.getLocationByLatitudeAndLongitude(48.208_49, 16.372_08))
                .thenReturn(Optional.empty());

        when(apiRequestService.retrieveAirportData(any())).thenReturn("[{\"coordinate_distance\":0.22067019115412875," +
                "\"station\":{\"city\":\"Vienna\",\"elevation_ft\":600,\"elevation_m\":183,\"icao\":\"LOWW\"}}]");

        when(locationRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> locationService.createLocation("Vienna"));
    }

    @Test
    void getAllLocations_ManyEntries() {
        final Location location1 = new Location();
        location1.setName("Vienna,Austria");

        final Location location2 = new Location();
        location2.setName("Graz,Austria");

        when(locationRepository.findAll()).thenReturn(List.of(location1, location2));

        final List<Location> locations = locationService.getAllLocations();

        assertEquals(2, locations.size());
        assertEquals("Vienna,Austria", locations.getFirst().getName());
    }

    @Test
    void getAllLocations_Empty() {
        when(locationRepository.findAll()).thenReturn(List.of());

        final List<Location> locations = locationService.getAllLocations();

        assertEquals(0, locations.size());
    }

    @Test
    void getLocationById_EntryFound() throws EntityNotFoundException {
        final Location location = new Location();
        location.setId(1);
        location.setName("Vienna,Austria");
        location.setLatitude(48.208_49);
        location.setLongitude(16.372_08);
        location.setElevation(171.0);

        when(locationRepository.findById(1)).thenReturn(Optional.of(location));

        final Location result = locationService.getLocationById(1);

        assertEquals("Vienna,Austria", result.getName());
        assertEquals(48.208_49, result.getLatitude());
        assertEquals(16.372_08, result.getLongitude());
        assertEquals(171.0, result.getElevation());
    }

    @Test
    void getLocationById_NotExistingEntity() {
        when(locationRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> locationService.getLocationById(1));
    }

    @Test
    void deleteLocation_Success() throws EntityNotFoundException {
        when(locationRepository.existsById(1)).thenReturn(true);

        locationService.deleteLocation(1);

        verify(locationRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteLocation_ThrowsEntityNotFoundException() {
        when(locationRepository.existsById(1)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> locationService.deleteLocation(1));
    }
}
