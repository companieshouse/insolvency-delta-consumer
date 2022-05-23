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

  Scenario Outline: Consume the message and invoke the insolvency data endpoint successfully for the delete event

    Given Insolvency delta consumer service is running
    When a "<message>" with "<companyNumber>" is published to the topic "insolvency-delta" and consumed
    And a delete event message "<deleteMessage>" is published to the topic "insolvency-delta"
    Then verify the insolvency data endpoint is invoked successfully

    Examples:
      | message     | deleteMessage | companyNumber |
      | case_type_1 | case_delete   | 02877511      |