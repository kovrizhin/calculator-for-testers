*** Settings ***
Resource            ../resources/api.resource

Suite Setup         Run Keywords    Create Session For User    ${ALICE}
...                 AND    Create Anonymous Session
...                 AND    Create Session With Credentials    wrong-password    alice    wrong
Suite Teardown      Delete All Sessions


*** Test Cases ***
Me Returns Authenticated Username
    ${response}=    Get Me    alice    expected_status=200
    Should Be Equal    ${response.json()}[username]    alice

Me Without Credentials Is Unauthorized
    ${response}=    Get Me    anon    expected_status=401
    Should Be Equal    ${response.json()}[code]    UNAUTHORIZED

Me With Wrong Password Is Unauthorized
    ${response}=    Get Me    wrong-password    expected_status=401
    Should Be Equal    ${response.json()}[code]    UNAUTHORIZED
