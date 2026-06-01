---
globs: ["**/monthly-expense-app/**", "**/deployment/**"]
---

## Kubernetes / Helm Conventions

- Backend runs 2 replicas; frontend runs 1 replica (see values.yaml)
- Kubernetes liveness probe: GET /actuator/health/liveness
- Kubernetes readiness probe: GET /actuator/health/readiness
- ShedLock requires shedlock table — ensure DB init runs before backend starts
- Environment variables for DB credentials come from Kubernetes Secrets, not ConfigMaps
- PostgreSQL uses Bitnami subchart (v13.1.5) — check chart version before upgrading
- Backend JVM flags: UseContainerSupport and MaxRAMPercentage=75 are set in Dockerfile — do not override in Helm without reason