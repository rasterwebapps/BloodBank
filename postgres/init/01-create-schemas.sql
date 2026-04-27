-- Create the keycloak schema and grant the application user access.
-- This runs automatically when the postgres container is first initialized.
CREATE SCHEMA IF NOT EXISTS keycloak AUTHORIZATION bloodbank;
