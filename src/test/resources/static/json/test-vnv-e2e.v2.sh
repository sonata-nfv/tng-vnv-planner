#!/bin/bash

test_uuid=`curl -s http://pre-int-vnv-bcn.5gtango.eu:32002/api/v3/tests/descriptors/ | jq .[0] | jq .uuid`

service_uuid=`curl -s http://pre-int-vnv-bcn.5gtango.eu:32002/api/v3/services/ | jq .[0] | jq .uuid`


cat <<EOF > planner-input.json
{
  "id": 100,
  "test_plans": [
    {
      "description": "Test Plan Description: Request of an adhoc testPlan of stored TD,NSD [integrationtests at 17:37utc 2019/04/22]",
      "id": 8,
      "index": 3,
      "service_uuid": $service_uuid,
      "test_uuid": $test_uuid,
      "status": "INTEGRATION_TESTING",
    }
  ],
  "uuid": "45678987655"
}
EOF

curl -X POST -H "Content-type:application/json" --data-binary @planner-input.json pre-int-vnv-bcn.5gtango.eu:6100/api/v1/test-plans