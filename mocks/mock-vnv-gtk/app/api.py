"""
Copyright (c) 2015 SONATA-NFV, 2019 5GTANGO [, ANY ADDITIONAL AFFILIATION]
ALL RIGHTS RESERVED.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
nor the names of its contributors may be used to endorse or promote
products derived from this software without specific prior written
permission.

This work has been performed in the framework of the SONATA project,
funded by the European Commission under Grant number 671517 through
the Horizon 2020 and 5G-PPP programmes. The authors would like to
acknowledge the contributions of their colleagues of the SONATA
partner consortium (www.sonata-nfv.eu).

This work has been performed in the framework of the 5GTANGO project,
funded by the European Commission under Grant number 761493 through
the Horizon 2020 and 5G-PPP programmes. The authors would like to
acknowledge the contributions of their colleagues of the 5GTANGO
partner consortium (www.5gtango.eu).
"""

# Catalogue Service

# Import framework
from flask import Flask, jsonify
from flask_restful import Resource, Api, reqparse
from json import dumps

from documents_catalogue import tests

# Instantiate the app
app = Flask(__name__)
api = Api(app)



class Tests_list(Resource):
    def get(self):
        return tests

class Tests(Resource):
    def get(self, uuid):
        for test in tests:
            if(uuid == test["uuid"]):
                return test, 200
        return "test not found", 404

# Create routes
#api.add_resource(Descriptors, '/api/v3')
api.add_resource(Tests_list, '/api/v3/tests/descriptors')
api.add_resource(Tests, '/api/v3/tests/descriptors/<uuid>')

# Run the application
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)


