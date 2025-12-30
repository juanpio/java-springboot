# Order Service BDD Tests

This directory contains Behavior-Driven Development (BDD) tests using Cucumber and Gherkin syntax.

## Test Structure

### Feature Files (`src/test/resources/features/`)

1. **order-management.feature** - Core order management scenarios:
   - Creating orders (single and multiple items)
   - Handling insufficient stock
   - Retrieving orders (all and by user)
   - Updating order status
   - Canceling orders

2. **circuit-breaker.feature** - Resilience testing scenarios:
   - Product service availability/failure
   - User service availability/failure with fallback
   - Retry mechanism on transient failures
   - Circuit breaker opening after multiple failures

### Step Definitions (`src/test/java/com/microservices/order/bdd/`)

- **OrderManagementSteps.java** - Step definitions for order management scenarios
- **CircuitBreakerSteps.java** - Step definitions for circuit breaker scenarios
- **CucumberSpringConfiguration.java** - Spring Boot test configuration
- **CucumberTestRunner.java** - JUnit Platform test runner
- **TestHooks.java** - Setup and teardown hooks

## Running the Tests

### Run all BDD tests:
```bash
cd order-service
mvn test
```

### Run specific feature:
```bash
mvn test -Dcucumber.filter.tags="@order-management"
```

### Generate HTML report:
After running tests, open: `target/cucumber-reports/cucumber.html`

## Test Configuration

- **application-test.properties** - Test-specific configuration
  - In-memory H2 database
  - Eureka disabled
  - Circuit breaker thresholds optimized for testing
  - Debug logging enabled

## Writing New Tests

### Add a new scenario to existing feature:
```gherkin
Scenario: Your scenario name
  Given some precondition
  When some action occurs
  Then expect this outcome
```

### Create a new feature file:
1. Create `*.feature` file in `src/test/resources/features/`
2. Add step definitions in corresponding Steps class
3. Run tests to verify

## Key Testing Patterns

### Data Tables
```gherkin
When I create an order with the following items:
  | productId | quantity |
  | 1         | 2        |
  | 2         | 3        |
```

### Parameterized Steps
```gherkin
Given a user exists with id 1 and username "john_doe"
Then the order total amount should be 999.99
```

### Mocking External Services
Circuit breaker tests use Mockito to mock ProductClient and UserClient:
```java
@MockBean
private ProductClient productClient;

when(productClient.getProductById(anyLong()))
    .thenThrow(new RuntimeException("Service down"));
```

## Test Coverage

Current scenarios cover:
- ✅ Order creation (happy path)
- ✅ Order creation (validation failures)
- ✅ Order retrieval (all, by user, by id)
- ✅ Order status updates
- ✅ Order cancellation (with business rules)
- ✅ Circuit breaker activation
- ✅ Retry mechanism
- ✅ Fallback methods
- ✅ Circuit breaker state transitions

## Dependencies

- **Cucumber Java** - BDD framework
- **Cucumber Spring** - Spring integration
- **REST Assured** - API testing
- **Mockito** - Mocking framework
- **JUnit Platform** - Test execution

## Best Practices

1. **One scenario per business rule** - Keep scenarios focused
2. **Use Background** - Set up common preconditions
3. **Descriptive names** - Scenario names should be self-explanatory
4. **Given-When-Then** - Follow BDD structure consistently
5. **Avoid technical details** - Feature files should be readable by non-technical stakeholders
6. **Reusable steps** - Write generic step definitions that can be reused

## Troubleshooting

### Tests fail with "Connection refused"
- Ensure `@SpringBootTest(webEnvironment = RANDOM_PORT)` is configured
- Check that test profile is active

### Circuit breaker not activating
- Check `application-test.properties` configuration
- Verify `minimumNumberOfCalls` threshold is met
- Check if retry is exhausted before circuit breaker activates

### Mocks not working
- Ensure `@MockBean` is used in step definition classes
- Verify `CucumberSpringConfiguration` is properly configured
- Check that mock setup happens before the tested action
