Feature: Create a hike
  Scenario: Successfully create a hike
    Given an authenticated hiking organizer
    And the organizer certification is valid on the planned date
    And the trail permits hiking during its opening season
    And the organizer level is sufficient for the trail difficulty
    When the organizer submits valid hike information
    Then the hike is saved with a unique identifier
    And the hike status is "open"
    And the creator is recorded in the lifecycle history

  Scenario: Reject a hike outside the trail opening season
    Given an authenticated hiking organizer
    And the trail is not open on the planned date
    When the organizer attempts to create the hike
    Then the hike is not created
    And a validation error is returned
