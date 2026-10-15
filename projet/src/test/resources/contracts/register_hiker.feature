Feature: Register a hiker
  Scenario: Register with a valid invitation code
    Given a valid unused invitation code
    And the submitted email is not already registered
    And the waiver requirements are satisfied
    When the person submits valid hiker information
    Then a hiker account is created
    And the membership status is "active"

  Scenario: Reject an invalid invitation code
    Given an invalid invitation code
    When a person submits the registration form
    Then no hiker account is created
    And a validation error is returned
