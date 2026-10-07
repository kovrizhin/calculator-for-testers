import os

BASE_URL = os.environ.get("CALC_BASE_URL", "http://localhost:8080")

ALICE = {"username": "alice", "password": "alice123"}
BOB = {"username": "bob", "password": "bob123"}
CAROL = {"username": "carol", "password": "carol123"}

HIGH_PRIORITY_TIMEOUT = "5s"
MEDIUM_PRIORITY_TIMEOUT = "10s"
LOW_PRIORITY_TIMEOUT = "20s"
