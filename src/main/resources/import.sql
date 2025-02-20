INSERT INTO users (id, firstname, lastname) VALUES (0, 'Jane', 'Doe') ON CONFLICT (id) DO NOTHING;

-- Testdata
INSERT INTO locations (id, name, latitude, longitude, elevation, icao)
    VALUES (1, 'Vienna,Austria', 48.2082, 16.3738, 151, 'LOWW')
        ON CONFLICT (id) DO NOTHING;
INSERT INTO favorites (name, user_id, location_id)
    VALUES ('Wien', 0, 1);