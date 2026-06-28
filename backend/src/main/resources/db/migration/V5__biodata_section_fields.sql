-- Add columns for form fields that previously had no backend home, so nothing entered in the app
-- is dropped on sync (maritalStatus, educationField, address/postalCode, sunSign) and so wealth
-- status and interests get dedicated columns instead of being overloaded onto familyValues/hobbies.
ALTER TABLE personal_details ADD COLUMN marital_status  VARCHAR(30);
ALTER TABLE family_details   ADD COLUMN family_status   VARCHAR(30);
ALTER TABLE education_career ADD COLUMN education_field  VARCHAR(100);
ALTER TABLE lifestyle        ADD COLUMN interests        TEXT;
ALTER TABLE astrology        ADD COLUMN sun_sign         VARCHAR(50);
ALTER TABLE contact_info     ADD COLUMN address          TEXT;
ALTER TABLE contact_info     ADD COLUMN postal_code      VARCHAR(20);
