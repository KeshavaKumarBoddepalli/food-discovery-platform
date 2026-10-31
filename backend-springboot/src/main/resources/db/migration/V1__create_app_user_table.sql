CREATE TABLE app_user (
    id BINARY(16) NOT NULL,
    google_subject VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    profile_image_url VARCHAR(2048),
    application_role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    contribution_points_cache BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT uk_app_user_google_subject UNIQUE (google_subject),
    CONSTRAINT ck_app_user_role CHECK (application_role IN ('USER', 'MODERATOR', 'ADMIN')),
    CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT ck_app_user_contribution_points CHECK (contribution_points_cache >= 0)
);
