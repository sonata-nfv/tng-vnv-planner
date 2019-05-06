#!/bin/bash

testd_uuid=`curl -s http://pre-int-vnv-bcn.5gtango.eu:32002/api/v3/tests/descriptors/ | jq .[0] | jq .uuid`

nsd_uuid=`curl -s http://pre-int-vnv-bcn.5gtango.eu:32002/api/v3/services/ | jq .[0] | jq .uuid`

date_stamp=`date +"%Y-%m-%d %H:%M:%S"`

cat <<EOF > planner-input.json
{
      "description": "Request of an adhoc testPlan of stored TD,NSD [integration tests at $date_stamp]",
      "nsd_uuid": $nsd_uuid,
      "testd_uuid": $testd_uuid,
      "status": "INTEGRATION_TESTING"
}
EOF

curl -X POST -H "Content-type:application/json" --data-binary @planner-input.json pre-int-vnv-bcn.5gtango.eu:6100/api/v1/test-plans