Feature: Send hike reminders
  Scenario: Send a reminder with a weather warning
    Given a non-cancelled hike is scheduled for tomorrow
    And confirmed registrations exist
    And the weather service forecasts rain or snow
    When the reminder process runs
    Then each confirmed hiker is sent a reminder
    And the reminder includes a weather warning

  Scenario: Send a reminder when weather data is unavailable
    Given a non-cancelled hike is scheduled for tomorrow
    And the weather service is unavailable
    When the reminder process runs
    Then each confirmed hiker is sent a reminder without a weather warning
