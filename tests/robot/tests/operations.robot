*** Settings ***
Resource            ../resources/api.resource

Suite Setup         Create Session For User    ${ALICE}
Suite Teardown      Delete All Sessions


*** Variables ***
${SAMPLE_EQUATION}      6+7+(3+4*5) * 3
${SAMPLE_RESULT}        82.0


*** Test Cases ***
Submit High Priority Equation And Read Result
    ${submitted}=    Submit Equation    alice    ${SAMPLE_EQUATION}    HIGH
    Should Be True    200 <= ${submitted.status_code} < 300
    ${accepted}=    Set Variable    ${submitted.json()}
    Should Not Be Empty    ${accepted}[id]
    Should Be Equal    ${accepted}[status]    PENDING
    Should Be Equal    ${accepted}[priority]    HIGH

    ${operation}=    Wait Until Operation Completes    alice    ${accepted}[id]
    Should Be Equal    ${operation}[status]    DONE
    Should Be Equal As Numbers    ${operation}[result]    ${SAMPLE_RESULT}
    Should Be Equal    ${operation}[equation]    ${SAMPLE_EQUATION}
    Should Be Equal    ${operation}[error]    ${None}
    Should Not Be Empty    ${operation}[completedAt]

Operation Is Pending Right After Submission
    ${submitted}=    Submit Equation    alice    1+1    LOW
    ${response}=    Get Operation    alice    ${submitted.json()}[id]    expected_status=202
    ${pending}=    Set Variable    ${response.json()}
    Should Be Equal    ${pending}[id]    ${submitted.json()}[id]
    List Should Contain Value    ${{ ["PENDING", "IN_PROGRESS"] }}    ${pending}[status]
    Should Be True    ${pending}[retryAfterSeconds] >= 1

Invalid Equation Is Rejected
    ${response}=    Submit Equation    alice    2++3    expected_status=400
    Should Be Equal    ${response.json()}[code]    INVALID_EQUATION
    Should Be Equal As Integers    ${response.json()}[position]    2

Blank Equation Is Rejected
    ${response}=    Submit Equation    alice    ${SPACE}    expected_status=400
    Should Be Equal    ${response.json()}[code]    VALIDATION_ERROR

Submitted Operation Appears In List
    ${submitted}=    Submit Equation    alice    2*3    HIGH
    ${response}=    List Operations    alice    expected_status=200
    ${page}=    Set Variable    ${response.json()}
    Should Be True    ${page}[total] >= 1
    ${ids}=    Evaluate    [item["id"] for item in $page["items"]]
    List Should Contain Value    ${ids}    ${submitted.json()}[id]
