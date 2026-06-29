-- Blood group is a personal physical attribute (lives on personal_details per the ERD),
-- optional and short (e.g. "AB+"). Existing rows default to NULL.
ALTER TABLE personal_details ADD COLUMN blood_group VARCHAR(5);
