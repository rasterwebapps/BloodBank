rootProject.name = "BloodBank"

// Shared Libraries
include("shared-libs:common-model")
include("shared-libs:common-dto")
include("shared-libs:common-events")
include("shared-libs:common-exceptions")
include("shared-libs:common-security")
include("shared-libs:db-migration")

// Integration Tests
include("backend:integration-tests")

// Backend Services
include("backend:api-gateway")
include("backend:config-server")
include("backend:donor-service")
include("backend:inventory-service")
include("backend:lab-service")
include("backend:branch-service")
include("backend:transfusion-service")
include("backend:hospital-service")
include("backend:billing-service")
include("backend:request-matching-service")
include("backend:notification-service")
include("backend:reporting-service")
include("backend:document-service")
include("backend:compliance-service")
