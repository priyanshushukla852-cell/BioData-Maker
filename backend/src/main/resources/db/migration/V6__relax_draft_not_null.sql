-- Offline-first drafts sync continuously and partially: the app pushes a section as soon as any
-- field in it is filled. The original NOT NULL constraints (full_name, dob, gender, diet,
-- highest_qualification, contact_phone/city/state/country) rejected those partial upserts with a
-- 500, so draft data never reached the server and AI summaries came back empty. Relax them to
-- nullable; completeness is enforced at the "Done"/export stage instead of on every draft sync.
ALTER TABLE personal_details ALTER COLUMN full_name             DROP NOT NULL;
ALTER TABLE personal_details ALTER COLUMN dob                   DROP NOT NULL;
ALTER TABLE personal_details ALTER COLUMN gender                DROP NOT NULL;
ALTER TABLE education_career ALTER COLUMN highest_qualification DROP NOT NULL;
ALTER TABLE lifestyle        ALTER COLUMN diet                  DROP NOT NULL;
ALTER TABLE contact_info     ALTER COLUMN contact_phone         DROP NOT NULL;
ALTER TABLE contact_info     ALTER COLUMN city                  DROP NOT NULL;
ALTER TABLE contact_info     ALTER COLUMN state                 DROP NOT NULL;
ALTER TABLE contact_info     ALTER COLUMN country               DROP NOT NULL;
