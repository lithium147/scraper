#!/usr/bin/env bash

source env.sh

# --locations=$SECRET_REGION --replication-policy=user-managed

echo "<TOKEN-IN-HERE>" \
        | gcloud secrets create 'influx-token' --project="$GCP_PROJECT" --data-file=-

#        --member="serviceAccount:777899678568-compute@developer.gserviceaccount.com" \
#        --member="user:lithium147@gmail.com" \
# XXX not sure if this is required - since it was not the reason for the original failure
gcloud secrets add-iam-policy-binding 'influx-token' --project="$GCP_PROJECT" \
        --member="serviceAccount:service-777899678568@dataflow-service-producer-prod.iam.gserviceaccount.com" \
        --role='roles/secretmanager.secretAccessor'
