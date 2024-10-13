CREATE TABLE asks (
    id CHAR(36) DEFAULT (UUID()),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    question VARCHAR(255) NOT NULL,
    answer VARCHAR(255) NULL,
    room_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    PRIMARY KEY(id)
);