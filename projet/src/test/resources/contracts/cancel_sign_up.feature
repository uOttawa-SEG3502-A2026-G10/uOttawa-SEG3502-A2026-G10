Feature: Cancel a hike registration
  Scenario: Cancel a confirmed registration and promote the first waitlisted hiker
    Given a hike has a confirmed registration and a non-empty waitlist
    And the registration deadline has not passed
    When the hiker confirms cancellation
    Then the registration status becomes "cancelled"
    And the first waitlisted hiker is promoted to confirmed
    And the promoted hiker is notified

  Scenario: Reject cancellation after the hike is closed
    Given the hike is closed
    When the hiker requests cancellation
    Then the cancellation is rejected
