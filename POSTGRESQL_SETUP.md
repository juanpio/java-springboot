# PostgreSQL Database Setup

## Overview

All three microservices (User, Product, and Order) now use PostgreSQL for persistent data storage instead of H2 in-memory databases.

## Database Configuration

### PostgreSQL Instances

Each service has its own PostgreSQL database running in Docker:

| Service | Database | Container | Port (Host) | Username | Password |
|---------|----------|-----------|-------------|----------|----------|
| User Service | userdb | postgres-user | 5432 | userservice | userpass123 |
| Product Service | productdb | postgres-product | 5433 | productservice | productpass123 |
| Order Service | orderdb | postgres-order | 5434 | orderservice | orderpass123 |

### Data Persistence

All databases use Docker volumes for persistent storage:
- `postgres-user-data` - User service data
- `postgres-product-data` - Product service data
- `postgres-order-data` - Order service data

**Data persists across container restarts** unless you explicitly remove the volumes with `docker-compose down -v`

## Running the Application

### Full Stack (Services + Databases)

```bash
# Build all services
mvn clean package -DskipTests

# Start everything
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f user-service
docker-compose logs -f product-service
docker-compose logs -f order-service

# Stop everything
docker-compose down

# Stop and remove volumes (deletes all data)
docker-compose down -v
```

### Databases Only (for Local Development)

If you want to run services locally but use PostgreSQL databases:

```bash
# Start only PostgreSQL databases
docker-compose -f docker-compose-postgres.yml up -d

# Run services locally
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run

# Stop databases
docker-compose -f docker-compose-postgres.yml down
```

## Accessing PostgreSQL

### Using psql CLI

```bash
# User database
docker exec -it postgres-user psql -U userservice -d userdb

# Product database
docker exec -it postgres-product psql -U productservice -d productdb

# Order database
docker exec -it postgres-order psql -U orderservice -d orderdb
```

### Common PostgreSQL Commands

```sql
-- List all tables
\dt

-- Describe table structure
\d users
\d products
\d orders

-- Query data
SELECT * FROM users;
SELECT * FROM products;
SELECT * FROM orders;

-- Count records
SELECT COUNT(*) FROM users;

-- Exit
\q
```

### Using pgAdmin or Database Client

Connect using these credentials:

**User Service:**
- Host: localhost
- Port: 5432
- Database: userdb
- Username: userservice
- Password: userpass123

**Product Service:**
- Host: localhost
- Port: 5433
- Database: productdb
- Username: productservice
- Password: productpass123

**Order Service:**
- Host: localhost
- Port: 5434
- Database: orderdb
- Username: orderservice
- Password: orderpass123

## Application Configuration

### Local Development (application.yml)

Services connect to localhost with different ports:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb  # or 5433, 5434
    driver-class-name: org.postgresql.Driver
    username: userservice
    password: userpass123
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

### Docker Environment (docker-compose.yml)

Environment variables override the configuration:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-user:5432/userdb
  - SPRING_DATASOURCE_USERNAME=userservice
  - SPRING_DATASOURCE_PASSWORD=userpass123
  - SPRING_DATASOURCE_DRIVER-CLASS-NAME=org.postgresql.Driver
  - SPRING_JPA_DATABASE-PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

## Database Schema

Hibernate automatically creates and updates tables based on entity classes:
- `ddl-auto: update` - Updates schema without dropping existing data
- Tables are created on first startup
- Schema changes are applied automatically

## Testing

### Unit Tests

Tests use H2 in-memory database (scope: test):
```bash
mvn test
```

### Integration Tests

BDD tests in `order-service` use H2:
```bash
cd order-service
mvn test -Dtest=CucumberTestRunner
```

### Testing with PostgreSQL

Use the Test.http file with Docker Compose running:
```bash
# Start services
docker-compose up -d

# Use Test.http to execute API requests
# Data will persist in PostgreSQL volumes
```

## Troubleshooting

### Connection Refused

If services can't connect to PostgreSQL:
```bash
# Check PostgreSQL health
docker-compose ps

# View PostgreSQL logs
docker logs postgres-user
docker logs postgres-product
docker logs postgres-order

# Ensure databases are healthy before starting services
```

### Data Not Persisting

Check if volumes are being removed:
```bash
# List volumes
docker volume ls | grep postgres

# Don't use -v flag to preserve data
docker-compose down  # Good - keeps data
docker-compose down -v  # Bad - deletes data
```

### Port Conflicts

If ports 5432, 5433, or 5434 are already in use:
```bash
# Check what's using the port
lsof -i :5432
lsof -i :5433
lsof -i :5434

# Stop conflicting services or change ports in docker-compose.yml
```

### Reset Database

To start fresh:
```bash
# Stop and remove everything including data
docker-compose down -v

# Restart
docker-compose up -d
```

## Migration from H2

Previous setup used H2 in-memory databases. Key changes:

1. **Dependency**: Added `postgresql` runtime dependency
2. **Driver**: Changed from `org.h2.Driver` to `org.postgresql.Driver`
3. **Dialect**: Changed from `H2Dialect` to `PostgreSQLDialect`
4. **URL**: Changed from `jdbc:h2:mem:*` to `jdbc:postgresql://host:port/db`
5. **Persistence**: Data now survives restarts

## Security Notes

**⚠️ Warning**: Default passwords are used for development only!

For production:
- Use strong passwords
- Store credentials in secrets management (e.g., Azure Key Vault, AWS Secrets Manager)
- Use environment-specific configurations
- Enable SSL/TLS connections
- Restrict database access
- Regular backups

## Backup and Restore

### Backup

```bash
# Backup user database
docker exec postgres-user pg_dump -U userservice userdb > backup_user.sql

# Backup product database
docker exec postgres-product pg_dump -U productservice productdb > backup_product.sql

# Backup order database
docker exec postgres-order pg_dump -U orderservice orderdb > backup_order.sql
```

### Restore

```bash
# Restore user database
cat backup_user.sql | docker exec -i postgres-user psql -U userservice -d userdb

# Restore product database
cat backup_product.sql | docker exec -i postgres-product psql -U productservice -d productdb

# Restore order database
cat backup_order.sql | docker exec -i postgres-order psql -U orderservice -d orderdb
```

## Performance Tuning

For production, consider:
- Connection pooling (HikariCP is already configured)
- Database indexes on frequently queried columns
- Query optimization
- Connection pool size tuning
- PostgreSQL configuration (shared_buffers, work_mem, etc.)

## Monitoring

View database activity:
```bash
# Active connections
docker exec -it postgres-user psql -U userservice -d userdb -c "SELECT * FROM pg_stat_activity;"

# Database size
docker exec -it postgres-user psql -U userservice -d userdb -c "SELECT pg_size_pretty(pg_database_size('userdb'));"

# Table sizes
docker exec -it postgres-user psql -U userservice -d userdb -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;"
```

## Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
