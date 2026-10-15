Feature: Cancel a hike
  Scenario: Cancel a hike before it starts
    Given an authenticated authorized organizer or administrator
    And the hike has not started
    When the user submits a cancellation reason and confirms
    Then the hike status becomes "cancelled"
    And confirmed and waitlisted registrations become "cancelled"
    And affected hikers are notified with the reason

  Scenario: Reject cancellation after the hike has started
    Given the hike has already started
    When an organizer requests cancellation
    Then the cancellation is rejected
