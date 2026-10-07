*** Settings ***
Documentation       Keywords for the Tester Calculator REST API.
...                 Sessions are keyed by username; use ``anon`` for unauthenticated calls.

Library             RequestsLibrary
Library             Collections
Variables           ../variables.py


*** Keywords ***
Create Session For User
    [Documentation]    Creates a RequestsLibrary session authenticated with HTTP Basic as ``user``.
    [Arguments]    ${user}
    ${auth}=    Create List    ${user}[username]    ${user}[password]
    Create Session    ${user}[username]    ${BASE_URL}    auth=${auth}
    RETURN    ${user}[username]

Create Session With Credentials
    [Documentation]    Creates a session with explicit credentials under the given alias.
    [Arguments]    ${alias}    ${username}    ${password}
    ${auth}=    Create List    ${username}    ${password}
    Create Session    ${alias}    ${BASE_URL}    auth=${auth}

Create Anonymous Session
    Create Session    anon    ${BASE_URL}

Submit Equation
    [Documentation]    POST /api/operations. ``priority`` is omitted from the body when not given.
    [Arguments]    ${alias}    ${equation}    ${priority}=${None}    ${expected_status}=any
    ${body}=    Create Dictionary    equation=${equation}
    IF    $priority is not None    Set To Dictionary    ${body}    priority=${priority}
    ${response}=    POST On Session    ${alias}    /api/operations    json=${body}    expected_status=${expected_status}
    RETURN    ${response}

Get Operation
    [Arguments]    ${alias}    ${id}    ${expected_status}=any
    ${response}=    GET On Session    ${alias}    /api/operations/${id}    expected_status=${expected_status}
    RETURN    ${response}

List Operations
    [Arguments]    ${alias}    ${page}=0    ${size}=20    ${expected_status}=any
    ${params}=    Create Dictionary    page=${page}    size=${size}
    ${response}=    GET On Session    ${alias}    /api/operations    params=${params}    expected_status=${expected_status}
    RETURN    ${response}

Delete Operation
    [Arguments]    ${alias}    ${id}    ${expected_status}=any
    ${response}=    DELETE On Session    ${alias}    /api/operations/${id}    expected_status=${expected_status}
    RETURN    ${response}

Get Me
    [Arguments]    ${alias}    ${expected_status}=any
    ${response}=    GET On Session    ${alias}    /api/me    expected_status=${expected_status}
    RETURN    ${response}

Operation Should Be Finished
    [Documentation]    Passes once GET returns 200 (status DONE or FAILED) and returns the body.
    [Arguments]    ${alias}    ${id}
    ${response}=    Get Operation    ${alias}    ${id}
    Should Be Equal As Integers    ${response.status_code}    200
    RETURN    ${response.json()}

Wait Until Operation Completes
    [Documentation]    Polls GET /api/operations/{id} until it answers 200 and returns the JSON body.
    [Arguments]    ${alias}    ${id}    ${timeout}=${HIGH_PRIORITY_TIMEOUT}
    ${operation}=    Wait Until Keyword Succeeds    ${timeout}    500ms    Operation Should Be Finished    ${alias}    ${id}
    RETURN    ${operation}

Submit And Wait
    [Documentation]    Submits an equation and returns the finished operation body.
    [Arguments]    ${alias}    ${equation}    ${priority}=HIGH    ${timeout}=${HIGH_PRIORITY_TIMEOUT}
    ${submitted}=    Submit Equation    ${alias}    ${equation}    ${priority}
    Should Be True    200 <= ${submitted.status_code} < 300
    ${operation}=    Wait Until Operation Completes    ${alias}    ${submitted.json()}[id]    ${timeout}
    RETURN    ${operation}
