-- Phase 1 baseline.
-- This migration only proves that Flyway is wired up (Flyway also creates its
-- flyway_schema_history table when it runs). Real tables start in Phase 2.
COMMENT ON SCHEMA public IS 'HealthPay application schema. Managed by Flyway migrations only.';
