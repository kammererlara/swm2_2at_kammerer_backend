package hs_burgenland.weather.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WeatherRecord {
    private LocalDateTime time;
    private double temperature;
    private int humidity;

}
