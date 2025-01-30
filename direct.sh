#!/usr/bin/env bash

mvn compile exec:java -Dexec.mainClass=com.solubris.Scrape \
-Dexec.args="--runner=DirectRunner \
--url=https://www.rhs.org.uk/shows-events/rhs-chelsea-flower-show/ticket-options" \
-Pdirect-runner
