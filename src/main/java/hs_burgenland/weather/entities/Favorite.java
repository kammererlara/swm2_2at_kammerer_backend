package hs_burgenland.weather.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "locationId"}),
        @UniqueConstraint(columnNames = {"userId", "name"})
})
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String name;
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}
