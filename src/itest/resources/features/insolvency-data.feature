Feature: Process insolvency delta information

  Scenario: Processing insolvency delta information successfully

    Given Insolvency delta consumer service is running
    When a message is published to the topic "insolvency-delta" and consumed
    Then verify PUT method is called on insolvency-data-api service
