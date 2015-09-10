#!/bin/bash

curl --verbose --data-ascii @bin/request.json --header 'X-Correlation-Id: foo' --header 'Content-Type: application/json' localhost:8000
