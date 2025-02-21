INSERT INTO users (id, firstname, lastname) VALUES (0, 'Jane', 'Doe') ON CONFLICT (id) DO NOTHING;

-- Testdata
INSERT INTO location (id, name, latitude, longitude, elevation, icao)
    VALUES (1, 'Vienna,Austria', 48.20849, 16.37208, 171, 'LOWW')
        ON CONFLICT (id) DO NOTHING;
INSERT INTO favorite (name, userId, locationId)
    VALUES ('Wien', 0, 1);