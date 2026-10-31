ALTER TABLE app_user
    ADD COLUMN display_name_customized BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE dish
    ADD COLUMN created_by BINARY(16) NULL,
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE dish
    ADD CONSTRAINT fk_dish_created_by FOREIGN KEY (created_by) REFERENCES app_user (id);

CREATE INDEX ix_dish_created_by_created_at ON dish (created_by, created_at);