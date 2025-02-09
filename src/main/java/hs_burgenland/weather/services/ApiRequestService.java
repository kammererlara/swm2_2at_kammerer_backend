package hs_burgenland.weather.services;

import hs_burgenland.weather.entities.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ApiRequestService {
    @Value("${url.geocoding}")
    private String geocodingUrl;
    @Value("${url.avwx}")
    private String avwxUrl;
    @Value("${token.avwx}")
    private String avwxToken;
    final private WebClient webClient;

    public ApiRequestService() {
        this.webClient = WebClient.create();
    }

    public String retrieveLocationData(final Location location) {
        return webClient
                .get()
                .uri(geocodingUrl + "?name=" + location.getName() + "&count=1")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String retrieveAirportData(final Location location) {
        return webClient
                .get()
                .uri(avwxUrl
                        + location.getLatitude() + "," + location.getLongitude()
                        + "?n=1&token=" + avwxToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
