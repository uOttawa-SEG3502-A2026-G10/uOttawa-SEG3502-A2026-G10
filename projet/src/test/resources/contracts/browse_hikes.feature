Feature: Browse hikes
  Scenario: Browse open hikes sorted by date
    Given open hikes exist in the system
    When a visitor browses all open hikes sorted by date
    Then the system displays only open hikes in date order
    And each result includes hike details, organizer, and remaining places

  Scenario: Filter open hikes by trail and difficulty
    Given open hikes exist for different trails and difficulty levels
    When a visitor searches by trail name and difficulty
    Then only matching open hikes are displayed
