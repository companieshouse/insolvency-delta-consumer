Feature: Process insolvency delta happy path scenario

  Scenario Outline: Consume the message and invoke the insolvency data endpoint successfully for the delete event

    Given Insolvency delta consumer service is running
    When a delete event message "<deleteMessage>" with "<companyNumber>" is published to the topic "insolvency-delta"
    Then verify the insolvency data endpoint is invoked successfully

    Examples:
      | message     | deleteMessage | companyNumber |
      | case_type_1 | case_delete   | 02877511      |


  Scenario Outline: Processing invalid message for delete event

    Given Insolvency delta consumer service is running
    When a non-avro "<input>" is published and failed to process
    Then the message should be moved to topic "insolvency-delta-insolvency-delta-consumer-invalid"

    Examples:
      | input               |
      | case_delete_invalid |

  Scenario Outline: Handle 4xx and 5xx error code when a delete event is sent

    Given Insolvency delta consumer service is running
    When a "<deleteMessage>" delete event with "<companyNumber>" is published to the topic "insolvency-delta" with insolvency data endpoint returning "<statusCode>"
    Then the message should be moved to topic "<topic>"

    Examples:
      | input       | deleteMessage | companyNumber | statusCode | topic                                              |
      | case_type_1 | case_delete   | 02877511      | 404        | insolvency-delta-insolvency-delta-consumer-invalid   |
      | case_type_1 | case_delete   | 02877511      | 400        | insolvency-delta-insolvency-delta-consumer-invalid |
      | case_type_1 | case_delete   | 02877511      | 503        | insolvency-delta-insolvency-delta-consumer-error   |