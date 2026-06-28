-- Records rewarded-ad unlocks that grant a user extra AI summary generations for the day.
-- One row per verified AdMob SSV callback. transaction_id is unique so Google's SSV retries
-- (it may call the callback more than once) don't double-grant.
CREATE TABLE ai_ad_grants (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    transaction_id  VARCHAR(128) NOT NULL UNIQUE,
    granted_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Supports "how many ad grants does this user have today" lookups.
CREATE INDEX idx_ai_ad_grants_user_granted ON ai_ad_grants(user_id, granted_at);

-- Supports the daily summary-generation count (filters by generated_at, joins via biodata to user).
CREATE INDEX idx_ai_generations_type_generated ON ai_generations(generation_type, generated_at);
