

mvn compile exec:java -Dexec.mainClass=com.solubris.Scrape \
-Dexec.args="--runner=DataflowRunner --project=explore-447815 \
--url=https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options
--gcpTempLocation=gs://explore-temp-bucket/tmp \
--region=europe-west1" \
-Pdataflow-runner


--inputFile=gs://apache-beam-samples/shakespeare/* --output=gs://<your-gcs-bucket>/counts" \
