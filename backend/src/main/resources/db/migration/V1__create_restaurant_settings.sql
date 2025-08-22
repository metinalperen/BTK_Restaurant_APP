CREATE TABLE IF NOT EXISTS restaurant_settings (
	id SERIAL PRIMARY KEY,
	restaurant_name VARCHAR(255) NOT NULL,
	open_time TIME NOT NULL,
	close_time TIME NOT NULL,
	last_reservation_cutoff_minutes INTEGER NOT NULL,
	created_at TIMESTAMP NOT NULL,
	updated_at TIMESTAMP NOT NULL
); 