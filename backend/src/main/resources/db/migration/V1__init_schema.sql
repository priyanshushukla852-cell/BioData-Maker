CREATE TABLE users (
    id              UUID PRIMARY KEY,
    auth_provider   VARCHAR(10) NOT NULL CHECK (auth_provider IN ('GOOGLE', 'OTP')),
    firebase_uid    VARCHAR(128) NOT NULL UNIQUE,
    phone           VARCHAR(15) NULL,
    email           VARCHAR(255) NULL,
    display_name    VARCHAR(100) NOT NULL,
    language_pref   VARCHAR(2) NOT NULL DEFAULT 'EN' CHECK (language_pref IN ('HI', 'EN')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE templates (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    preview_url     TEXT NULL,
    style           VARCHAR(20) NOT NULL CHECK (style IN ('TRADITIONAL', 'MODERN', 'FLORAL', 'MINIMAL', 'ROYAL', 'FESTIVE')),
    is_premium      BOOLEAN NOT NULL DEFAULT false,
    supports_hindi  BOOLEAN NOT NULL DEFAULT true,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE biodatas (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    template_id     UUID NULL REFERENCES templates(id),
    title           VARCHAR(100) NOT NULL,
    language        VARCHAR(2) NOT NULL DEFAULT 'EN' CHECK (language IN ('HI', 'EN')),
    status          VARCHAR(10) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'DONE')),
    ai_summary      TEXT NULL,
    deleted_at      TIMESTAMPTZ NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_biodatas_user_id ON biodatas(user_id);
CREATE INDEX idx_biodatas_deleted_at ON biodatas(deleted_at);

CREATE TABLE personal_details (
    id              UUID PRIMARY KEY,
    biodata_id      UUID NOT NULL UNIQUE REFERENCES biodatas(id) ON DELETE CASCADE,
    full_name       VARCHAR(100) NOT NULL,
    dob             DATE NOT NULL,
    gender          VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    religion        VARCHAR(50) NULL,
    caste           VARCHAR(50) NULL,
    gotra           VARCHAR(50) NULL,
    height_cm       INT NULL,
    complexion      VARCHAR(30) NULL,
    disability      VARCHAR(100) NULL
);

CREATE TABLE family_details (
    id                  UUID PRIMARY KEY,
    biodata_id          UUID NOT NULL UNIQUE REFERENCES biodatas(id) ON DELETE CASCADE,
    father_name         VARCHAR(100) NULL,
    father_occupation   VARCHAR(100) NULL,
    mother_name         VARCHAR(100) NULL,
    mother_occupation   VARCHAR(100) NULL,
    siblings            TEXT NULL,
    family_type         VARCHAR(20) NULL,
    family_values       VARCHAR(50) NULL
);

CREATE TABLE education_career (
    id                  UUID PRIMARY KEY,
    biodata_id          UUID NOT NULL UNIQUE REFERENCES biodatas(id) ON DELETE CASCADE,
    highest_qualification VARCHAR(100) NOT NULL,
    college             VARCHAR(200) NULL,
    job_title           VARCHAR(100) NULL,
    company             VARCHAR(200) NULL,
    annual_income       VARCHAR(50) NULL,
    work_location       VARCHAR(100) NULL
);

CREATE TABLE lifestyle (
    id                  UUID PRIMARY KEY,
    biodata_id          UUID NOT NULL UNIQUE REFERENCES biodatas(id) ON DELETE CASCADE,
    diet                VARCHAR(10) NOT NULL CHECK (diet IN ('VEG', 'NONVEG')),
    drinking            VARCHAR(15) NULL CHECK (drinking IN ('NO', 'YES', 'OCCASIONALLY')),
    smoking             VARCHAR(15) NULL CHECK (smoking IN ('NO', 'YES', 'OCCASIONALLY')),
    hobbies             TEXT NULL,
    languages_spoken    TEXT NULL
);

CREATE TABLE astrology (
    id              UUID PRIMARY KEY,
    biodata_id      UUID NOT NULL UNIQUE REFERENCES biodatas(id) ON DELETE CASCADE,
    rashi           VARCHAR(50) NULL,
    nakshatra       VARCHAR(50) NULL,
    manglik         VARCHAR(10) NULL CHECK (manglik IN ('YES', 'NO', 'PARTIAL')),
    birth_time      TIME NULL,
    birth_place     VARCHAR(100) NULL
);

CREATE TABLE contact_info (
    id              UUID PRIMARY KEY,
    biodata_id      UUID NOT NULL UNIQUE REFERENCES biodatas(id) ON DELETE CASCADE,
    contact_phone   VARCHAR(15) NOT NULL,
    contact_email   VARCHAR(255) NULL,
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    country         VARCHAR(50) NOT NULL
);

CREATE TABLE biodata_photos (
    id              UUID PRIMARY KEY,
    biodata_id      UUID NOT NULL REFERENCES biodatas(id) ON DELETE CASCADE,
    photo_type      VARCHAR(10) NOT NULL CHECK (photo_type IN ('PROFILE', 'FAMILY', 'EXTRA')),
    storage_url     TEXT NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_biodata_photos_biodata_id ON biodata_photos(biodata_id);

CREATE TABLE ai_generations (
    id              UUID PRIMARY KEY,
    biodata_id      UUID NOT NULL REFERENCES biodatas(id) ON DELETE CASCADE,
    generation_type VARCHAR(20) NOT NULL CHECK (generation_type IN ('SUMMARY', 'FIELD_SUGGEST')),
    input_snapshot  JSONB NOT NULL,
    ai_response     TEXT NULL,
    field_name      VARCHAR(50) NULL,
    accepted        BOOLEAN NULL,
    generated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ai_generations_biodata_id ON ai_generations(biodata_id);

CREATE TABLE pdf_exports (
    id              UUID PRIMARY KEY,
    biodata_id      UUID NOT NULL REFERENCES biodatas(id) ON DELETE CASCADE,
    storage_url     TEXT NOT NULL,
    language        VARCHAR(2) NOT NULL CHECK (language IN ('HI', 'EN')),
    template_id     UUID NULL REFERENCES templates(id),
    exported_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pdf_exports_biodata_id ON pdf_exports(biodata_id);
