#!/usr/bin/env bash

source env.sh

mvn clean compile exec:java -Dexec.mainClass=com.solubris.Scrape \
-Dexec.args="--runner=DataflowRunner --project=$GCP_PROJECT \
--url=https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options \
--gcpTempLocation=$GCP_BUCKET/tmp \
--region=$GCP_REGION" \
-Pdataflow-runner
