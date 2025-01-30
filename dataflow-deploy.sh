#!/usr/bin/env bash

source env.sh

gcloud storage buckets create --project="$GCP_PROJECT" --location=$GCP_REGION "$GCP_BUCKET"
gcloud storage cp metadata.json "$GCP_BUCKET/scrape-1.0.0_metadata"

mvn clean compile exec:java -Dexec.mainClass=com.solubris.Scrape \
-Dexec.args="--runner=DataflowRunner --project=$GCP_PROJECT \
--url=https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options \
--gcpTempLocation=$GCP_BUCKET/tmp \
--region=$GCP_REGION \
--templateLocation=$GCP_BUCKET/scrape-1.0.0" \
-Pdataflow-runner

#gcloud storage cp metadata.json gs://explore-temp-bucket/scrape-1.0.0_metadata
#gcloud auth login
#gcloud config set project PROJECT_ID

