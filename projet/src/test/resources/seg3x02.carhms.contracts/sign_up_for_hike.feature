Feature: Sign up for a hike
  Scenario: Sign up when a place is available
    Given an authenticated hiker with active membership
    And an open hike has one remaining place
    And the hiker is eligible for the hike difficulty
    When the hiker confirms the sign-up
    Then a confirmed registration is recorded
    And the registration timestamp is recorded
    And the hike becomes complete

  Scenario: Join the waitlist when the hike is full
    Given an authenticated eligible hiker with active membership
    And the selected hike is complete
    When the hiker confirms the sign-up
    Then the registration status is "waitlisted"
    And the hiker receives their waitlist position
