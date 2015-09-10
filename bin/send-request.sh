#!/bin/bash

for i in `seq 1 100`;
do
    curl --verbose --data-ascii @bin/request.json --header "X-Correlation-Id: ${i}" --header 'Content-Type: application/json' localhost:8000
done

