Feature: Process insolvency delta information

  Scenario: Processing insolvency delta information successfully

    Given Insolvency delta consumer service is running
    When a message is published to the topic "insolvency-delta"
    Then the insolvency delta consumer should consume and process the message
