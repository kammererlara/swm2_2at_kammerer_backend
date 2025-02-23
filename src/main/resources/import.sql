INSERT INTO users (id, firstname, lastname) VALUES (1, 'Jane', 'Doe') ON CONFLICT (id) DO NOTHING;

-- Testdata
INSERT INTO location (id, name, latitude, longitude, elevation, icao) VALUES (1, 'Vienna,Austria', 48.20849, 16.37208, 171, 'LOWW') ON CONFLICT (id) DO NOTHING;
INSERT INTO favorite (name, user_id, location_id) VALUES ('Wien', 1, 1) ON CONFLICT (user_id, name) DO NOTHING;