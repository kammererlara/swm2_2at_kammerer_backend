MERGE INTO users
    USING (SELECT 0 AS id, 'Jane' AS firstname, 'Doe' AS lastname) AS vals
    ON users.id = vals.id
    WHEN NOT MATCHED THEN
        INSERT (id, firstname, lastname) VALUES (vals.id, vals.firstname, vals.lastname);

-- Testdata
INSERT INTO location (id, name, latitude, longitude, elevation, icao)
    VALUES (1, 'Vienna,Austria', 48.20849, 16.37208, 171, 'LOWW')
        ON CONFLICT (id) DO NOTHING;
INSERT INTO favorite (name, userId, locationId)
    VALUES ('Wien', 0, 1);