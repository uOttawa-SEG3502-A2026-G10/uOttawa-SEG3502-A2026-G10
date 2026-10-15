Feature: Update a hiker
  Scenario: Hiker updates personal information
    Given an authenticated hiker
    When the hiker submits valid updated personal information
    Then the hiker profile is updated

  Scenario: Administrator records organizer certification
    Given an authenticated administrator
    And an existing hiker
    When the administrator records a valid certification and organizer level
    Then the hiker is recognized as a hiking organizer
