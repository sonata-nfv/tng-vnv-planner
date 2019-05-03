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


