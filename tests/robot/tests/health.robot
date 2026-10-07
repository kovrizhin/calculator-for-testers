*** Settings ***
Resource            ../resources/api.resource

Suite Setup         Create Anonymous Session
Suite Teardown      Delete All Sessions


*** Test Cases ***
Health Is Public
    ${response}=    GET On Session    anon    /api/health    expected_status=200
    Should Be Equal    ${response.json()}[status]    UP
