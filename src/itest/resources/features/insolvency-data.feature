Feature: Process insolvency delta information

  Scenario Outline: Processing insolvency delta information successfully

    Given Insolvency delta consumer service is running
    When a "<message>" with "<companyNumber>" is published to the topic "insolvency-delta" and consumed
    Then verify PUT method is called on insolvency-data-api service

    Examples:
      | message     | companyNumber |
      | case_type_1 | 02877511      |
      | case_type_2 | 02877511      |
      | case_type_3 | 02877511      |
