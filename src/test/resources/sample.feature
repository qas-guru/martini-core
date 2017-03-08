# This is a comment.

@Classification("Reporting")
@Meta("Selenium")
Feature: Functionality of the Reporting Subsystem
  This is the basic description for my functionality.

  @Smoke
  @Meta("Firefox")
  Scenario: A Corner Case
    Given a pre-existing condition
    And an emergent condition
    When something happens
    Then assert expected outcome