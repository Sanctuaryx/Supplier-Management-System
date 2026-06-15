-- =============================================================
-- Seed data covering all workflow scenarios
-- =============================================================
-- Run against the running stack:
--   docker compose exec -T db psql -U supplier -d supplierdb < seed-data.sql
-- =============================================================

-- ----------------------------------------------------------------
-- SUPPLIERS  (represent completed candidacies already in the system)
-- ----------------------------------------------------------------

-- SPAIN (ES) — 5 active/probation + 1 banned
-- Bonus pool (2 lowest unique turnovers among non-DISQUALIFIED): 1,500,000 and 2,000,000
INSERT INTO suppliers (duns, name, country, annual_turnover, rating, status) VALUES
  (100000001, 'Textiles Madrid SA',       'ES', 5000000,  'A', 'ACTIVE'),        -- score: 500,000.00
  (100000002, 'Confección Barcelona SL',  'ES', 2000000,  'B', 'ACTIVE'),        -- score: 187,500.00 (+bonus)
  (100000003, 'Moda Valencia SL',         'ES', 1500000,  'C', 'ON_PROBATION'),  -- score:  93,750.00 (+bonus)
  (100000004, 'Sevillana Textil SA',      'ES', 3000000,  'D', 'ON_PROBATION'),  -- score:  75,000.00
  (100000005, 'Bilbao Fabrics SA',        'ES', 10000000, 'E', 'ON_PROBATION'),  -- score: 100,000.00
  (100000006, 'Banned Textiles SL',       'ES', 8000000,  'E', 'DISQUALIFIED'); -- excluded; trying to reapply → 409

-- FRANCE (FR) — 3 suppliers
-- Bonus pool: 1,200,000 and 1,800,000
INSERT INTO suppliers (duns, name, country, annual_turnover, rating, status) VALUES
  (200000001, 'Paris Couture SA',     'FR', 4000000, 'A', 'ACTIVE'),        -- score: 400,000.00
  (200000002, 'Lyon Fabric SARL',     'FR', 1200000, 'B', 'ACTIVE'),        -- score: 112,500.00 (+bonus)
  (200000003, 'Marseille Mode SARL',  'FR', 1800000, 'C', 'ON_PROBATION');  -- score: 112,500.00 (+bonus)

-- PORTUGAL (PT) — 2 suppliers; both get the bonus (exactly 2 unique turnovers in country)
INSERT INTO suppliers (duns, name, country, annual_turnover, rating, status) VALUES
  (300000001, 'Porto Textile Lda',  'PT', 1100000, 'A', 'ACTIVE'),  -- score: 137,500.00 (+bonus)
  (300000002, 'Lisboa Fashion Lda', 'PT', 2500000, 'B', 'ACTIVE');  -- score: 234,375.00 (+bonus)

-- ITALY (IT) — sole supplier in country; gets the bonus
INSERT INTO suppliers (duns, name, country, annual_turnover, rating, status) VALUES
  (400000001, 'Milano Style SpA', 'IT', 3500000, 'A', 'ACTIVE'); -- score: 437,500.00 (+bonus)

-- ----------------------------------------------------------------
-- CANDIDATES
-- ----------------------------------------------------------------

-- Accepted candidate for the DISQUALIFIED supplier
-- (simulates the supplier having gone through the full accept flow before being banned)
INSERT INTO candidates (duns, name, country, annual_turnover, status)
VALUES (100000006, 'Banned Textiles SL', 'ES', 8000000, 'ACCEPTED');

-- PENDING candidate: ready to be accepted or refused via the API
-- Annual turnover 2,000,000 satisfies the minimum for acceptance (≥ 1,000,000)
INSERT INTO candidates (duns, name, country, annual_turnover, status)
VALUES (500000001, 'Pending Corp SL', 'ES', 2000000, 'PENDING');

-- REFUSED candidate: no active candidacy → a new one CAN be submitted for this DUNS
INSERT INTO candidates (duns, name, country, annual_turnover, status)
VALUES (500000002, 'Refused Corp SARL', 'FR', 1500000, 'REFUSED');
