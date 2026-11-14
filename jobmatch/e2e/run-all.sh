#!/bin/bash
# Запускає обидва набори тестів функцій; перед кожним — застосунок зі свіжою БД (початкові дані з V2__seed.sql).
set -e
APP="$(cd "$(dirname "$0")/.." && pwd)"
cd "$APP"
[ -f target/jobmatch-1.0.0.jar ] || mvn -q -DskipTests package

start_fresh() {
  pkill -f jobmatch-1.0.0.jar || true
  sleep 1
  rm -rf "$APP/data"
  (cd "$APP" && java -jar target/jobmatch-1.0.0.jar > /tmp/jobmatch-e2e.log 2>&1 &)
  for i in $(seq 1 60); do curl -s -o /dev/null http://localhost:8090/ && return; sleep 1; done
  echo "JobMatch не запустився, див. /tmp/jobmatch-e2e.log"; exit 1
}

cd e2e
start_fresh; npx playwright test tests/r2-candidate-matching.spec.ts
start_fresh; npx playwright test tests/functions.spec.ts
pkill -f jobmatch-1.0.0.jar || true
