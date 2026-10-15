Feature: Close a hike
  Scenario: Close a hike when the registration deadline is reached
    Given a non-cancelled hike whose registration deadline has been reached
    When the scheduled closure process runs
    Then the hike status becomes "closed"
    And the lifecycle actor and timestamp are recorded
    And new registrations and cancellations are rejected
