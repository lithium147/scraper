mvn compile exec:java -Dexec.mainClass=com.solubris.Scrape \
-Dexec.args="--runner=DataflowRunner --project=explore-447815 \
--url=https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options \
--gcpTempLocation=gs://explore-temp-bucket/tmp \
--templateLocation=gs://explore-temp-bucket/scrape-1.0.0 \
--region=europe-west1" \
-Pdataflow-runner
