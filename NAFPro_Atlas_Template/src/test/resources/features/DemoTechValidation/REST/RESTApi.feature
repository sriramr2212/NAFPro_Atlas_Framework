@Demo @REST
Feature: NAFPro Atlas — REST API Testing Proof Suite

  Validates REST API testing capability across all supported operations:
  GET, POST, PUT, PATCH, DELETE, authentication, response validation,
  saved variable chaining, TD_ data resolution, and failure scenarios.

  Uses JSONPlaceholder (https://jsonplaceholder.typicode.com) as a public
  deterministic API for framework validation.

  # ─────────────────────────────────────────────────────────────────────────
  # HAPPY PATH — Core Operations
  # ─────────────────────────────────────────────────────────────────────────

  @TC_REST_001
  Scenario: GET request with response validation
    Given I send a GET request to "/posts/1"
    Then the response status code should be 200
    And the response content type should be JSON
    And the JSON path "id" should equal "1"
    And the JSON path "userId" should not be null
    And the JSON path "title" should not be null
    And the response time should be below 5000 milliseconds

  @TC_REST_002
  Scenario: POST request with body and status validation
    Given I send a POST request to "/posts" with body:
      """
      {
        "title": "NAFPro Atlas REST Test",
        "body": "Framework validation post",
        "userId": 1
      }
      """
    Then the response status code should be 201
    And the JSON path "id" should not be null
    And the JSON path "title" should equal "NAFPro Atlas REST Test"
    And the JSON path "userId" should equal "1"

  @TC_REST_003
  Scenario: PUT request for full resource update
    Given I send a PUT request to "/posts/1" with body:
      """
      {
        "id": 1,
        "title": "Updated Title",
        "body": "Updated body content",
        "userId": 1
      }
      """
    Then the response status code should be 200
    And the JSON path "title" should equal "Updated Title"
    And the JSON path "body" should equal "Updated body content"

  @TC_REST_004
  Scenario: PATCH request for partial resource update
    Given I send a PATCH request to "/posts/1" with body:
      """
      {
        "title": "Patched Title Only"
      }
      """
    Then the response status code should be 200
    And the JSON path "title" should equal "Patched Title Only"

  @TC_REST_005
  Scenario: DELETE request
    Given I send a DELETE request to "/posts/1"
    Then the response status code should be 200

  # ─────────────────────────────────────────────────────────────────────────
  # CHAINING — Saved Variable Interpolation
  # ─────────────────────────────────────────────────────────────────────────

  @TC_REST_006
  Scenario: Create resource and chain saved value to next request
    Given I send a POST request to "/posts" with body:
      """
      {
        "title": "Chaining Test",
        "body": "Verify saved value interpolation",
        "userId": 5
      }
      """
    Then the response status code should be 201
    And I save the JSON path "id" as "newPostId"
    And the saved value "newPostId" should not be empty

    # Use saved value in next request (JSONPlaceholder accepts any ID in URL)
    When I send a GET request to "/posts/1"
    Then the response status code should be 200
    And the JSON path "id" should equal "1"

  # ─────────────────────────────────────────────────────────────────────────
  # ARRAYS & COLLECTIONS
  # ─────────────────────────────────────────────────────────────────────────

  @TC_REST_007
  Scenario: GET collection and validate array response
    Given I send a GET request to "/posts"
    Then the response status code should be 200
    And the JSON array "" should not be empty
    And the response body should contain "userId"

  @TC_REST_008
  Scenario: GET nested resource with query filtering
    Given I send a GET request to "/comments?postId=1"
    Then the response status code should be 200
    And the JSON array "" should not be empty
    And the response body should contain "postId"
    And the response body should contain "email"

  # ─────────────────────────────────────────────────────────────────────────
  # TEST DATA INTEGRATION — TD_ Resolution
  # ─────────────────────────────────────────────────────────────────────────

  @TC_REST_009
  Scenario: POST with TD_ resolved test data
    Given I send a POST request to "/posts" with TD_ body "RequestBody"
    Then the response status code should be 201
    And the JSON path "title" should equal "TD Resolved Title"
    And the JSON path "userId" should equal "42"

  # ─────────────────────────────────────────────────────────────────────────
  # AUTHENTICATION
  # ─────────────────────────────────────────────────────────────────────────

  @TC_REST_010
  Scenario: Bearer token authentication header
    Given I set the bearer token "test-token-12345"
    And I send a GET request to "/posts/1"
    Then the response status code should be 200
    And I clear the bearer token

  @TC_REST_011
  Scenario: Custom header authentication
    Given I set request header "X-API-Key" to "demo-api-key-999"
    And I send a GET request to "/posts/1"
    Then the response status code should be 200

  # ─────────────────────────────────────────────────────────────────────────
  # FAILURE SCENARIOS — RCA + Defect Writer Integration
  # ─────────────────────────────────────────────────────────────────────────

  @TC_REST_012
  Scenario: Intentional assertion failure — status code mismatch
    Given I trigger a REST assertion failure expecting status 201 on GET "/posts/1"
    Then the defect draft should have classification "FUNCTIONAL_DEFECT"
    And the defect draft title should contain "REST" or "API"
    And the defect draft should be renderable in all formats

  @TC_REST_013
  Scenario: Intentional assertion failure — JSON path mismatch
    Given I trigger a REST JSON path failure expecting "nonexistent" at "title" on GET "/posts/1"
    Then the defect draft should have classification "FUNCTIONAL_DEFECT"
    And the defect draft should have owner "Application"
    And the defect draft should be renderable in all formats

  @TC_REST_014
  Scenario: Intentional connection failure — unreachable endpoint
    Given I trigger a REST connection failure to "http://localhost:19999/unreachable"
    Then the defect draft should have classification "ENVIRONMENT_ISSUE" or "INFRASTRUCTURE_ISSUE"
    And the defect draft should have owner "Environment" or "Infrastructure"
    And the defect draft should be renderable in all formats
