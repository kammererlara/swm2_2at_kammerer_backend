INSERT INTO users (id, firstname, lastname) VALUES (0, 'Jane', 'Doe') ON CONFLICT (id) DO NOTHING;
