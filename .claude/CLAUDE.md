# Monthly Expense Tracker

## Project Overview
Full-stack personal finance application with Spring Boot backend, PostgreSQL database, React frontend, and comprehensive containerization.

## Architecture
- **Backend**: Spring Boot 3.x with Java 17 (Amazon Corretto)
- **Database**: PostgreSQL with JPA/Hibernate
- **Frontend**: React 18+ (separate repository/service)
- **Infrastructure**: 
  - Multi-architecture Docker (AMD64/ARM64)
  - Kubernetes deployment with NGINX ingress
  - KIND for local development

## Key Backend Components
- **Controllers**: REST API endpoints for expense management
- **Services**: Business logic layer
- **Repositories**: JPA repositories for data persistence
- **Entities**: JPA entities for database mapping
- **DTOs**: Data transfer objects for API responses
- **Configuration**: Spring Boot configuration classes

## Current Features
- Expense CRUD operations
- Category management
- Multi-architecture Docker builds
- Kubernetes deployment manifests
- NGINX ingress configuration

## Development Workflow
- Maven for dependency management
- Spring Boot DevTools for hot reload
- Docker Compose for local development
- KIND for local Kubernetes testing
- Multi-stage Dockerfile for optimized builds

## Infrastructure Highlights
- ARM64 support for 20-40% cost savings on cloud
- Kubernetes-ready with proper health checks
- NGINX ingress for load balancing
- ConfigMap and Secret management

## Common Development Tasks
- Adding new REST endpoints
- Database schema migrations
- Docker image building and testing
- Kubernetes manifest updates
- Integration testing setup
- Performance optimization

## Tech Stack
- Java 17 (Amazon Corretto)
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Maven
- Docker & Docker Compose
- Kubernetes & KIND
- NGINX Ingress
