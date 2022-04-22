Feature: Process insolvency delta information

  Scenario Outline: Processing insolvency delta information successfully

    Given Insolvency delta consumer service is running
    When a "<message>" with "<companyNumber>" is published to the topic "insolvency-delta" and consumed
    Then verify PUT method is called on insolvency-data-api service with body "<output>"

    Examples:
      | message     | companyNumber | output             |
      | case_type_1 | 02877511      | case_type_1_output |
      | case_type_2 | 02877512      | case_type_2_output |
      | case_type_3 | 02877513      | case_type_3_output |

  Scenario: Processing invalid message

    Given Insolvency delta consumer service is running
    When a non-avro message is published and failed to process
    Then the message should be moved to topic "insolvency-delta-insolvency-delta-consumer-invalid"

  Scenario: Processing valid avro message with invalid json

    Given Insolvency delta consumer service is running
    When a valid message is published with invalid json
    Then the message should be moved to topic "insolvency-delta-insolvency-delta-consumer-invalid"

  Scenario Outline: Handle 400,503 errors from stubbed insolvency-data-api call

    Given Insolvency delta consumer service is running
    When a message "<input>" is published for "<companyNumber>" and stubbed insolvency-data-api returns "<statusCode>"
    Then the message should be moved to topic "<topic>"

    Examples:
      | input       | companyNumber | statusCode | topic                                              |
      | case_type_1 | 02877511      | 400        | insolvency-delta-insolvency-delta-consumer-invalid |
      | case_type_1 | 02877511      | 503        | insolvency-delta-insolvency-delta-consumer-error   |

  Scenario Outline: Handle NPE while processing insolvency delta information

    Given Insolvency delta consumer service is running
    When a message "<input>" is published for "<companyNumber>" with unexpected data
    Then the message should be moved to topic "<topic>"

    Examples:
      | input         | companyNumber | statusCode | topic                                            |
      | case_type_NPE | 02877511      | 400        | insolvency-delta-insolvency-delta-consumer-error |
