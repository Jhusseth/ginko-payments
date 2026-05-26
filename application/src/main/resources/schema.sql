CREATE TABLE IF NOT EXISTS providers
(
    id                        UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name                      VARCHAR(255) NOT NULL,
    tax_identification_number VARCHAR(100) NOT NULL UNIQUE,
    email                     VARCHAR(255) NOT NULL,
    status                    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS payment_orders
(
    id              UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    provider_id     UUID           NOT NULL,
    provider_name   VARCHAR(255)   NOT NULL,
    amount          DECIMAL(19, 2) NOT NULL,
    description     VARCHAR(500),
    creation_date   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date     TIMESTAMP,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    version         BIGINT                  DEFAULT 0,
    idempotency_key VARCHAR(255) UNIQUE,
    FOREIGN KEY (provider_id) REFERENCES providers (id)
);
