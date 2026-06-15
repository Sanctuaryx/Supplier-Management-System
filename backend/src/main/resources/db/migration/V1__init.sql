CREATE TABLE candidates (
    duns            INTEGER       NOT NULL PRIMARY KEY,
    name            VARCHAR(255)  NOT NULL,
    country         VARCHAR(2)    NOT NULL,
    annual_turnover BIGINT        NOT NULL CHECK (annual_turnover >= 0),
    status          VARCHAR(20)   NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REFUSED'))
);

CREATE TABLE suppliers (
    duns            INTEGER       NOT NULL PRIMARY KEY,
    name            VARCHAR(255)  NOT NULL,
    country         VARCHAR(2)    NOT NULL,
    annual_turnover BIGINT        NOT NULL CHECK (annual_turnover >= 0),
    rating          VARCHAR(1)    NOT NULL CHECK (rating IN ('A', 'B', 'C', 'D', 'E')),
    status          VARCHAR(20)   NOT NULL CHECK (status IN ('ACTIVE', 'ON_PROBATION', 'DISQUALIFIED'))
);

CREATE INDEX idx_suppliers_status_turnover
    ON suppliers (status, annual_turnover)
    WHERE status != 'DISQUALIFIED';

CREATE INDEX idx_suppliers_country_turnover
    ON suppliers (country, annual_turnover)
    WHERE status != 'DISQUALIFIED';
