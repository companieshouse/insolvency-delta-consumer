Feature: Process insolvency delta happy path scenario

  Scenario Outline: Processing insolvency delta information successfully

    Given Insolvency delta consumer service is running
    When a "<message>" with "<companyNumber>" is published to the topic "insolvency-delta" and consumed
    Then verify PUT method is called on insolvency-data-api service with body "<output>"

    Examples:
      | message     | companyNumber | output             |
      | case_type_1 | 02877511      | case_type_1_output |
      | case_type_2 | 02877512      | case_type_2_output |
      | case_type_3 | 02877513      | case_type_3_output |
      | case_type_4 | 02877514      | case_type_4_output |