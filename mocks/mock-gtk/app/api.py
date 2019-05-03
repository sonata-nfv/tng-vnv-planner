# Catalogue Service

# Import framework
from flask import Flask, jsonify
from flask_restful import Resource, Api, reqparse
from json import dumps

from documents_catalogue import services, packages

# Instantiate the app
app = Flask(__name__)
api = Api(app)


class Services_list(Resource):
    def get(self):
        return services

class Services(Resource):
    def get(self, uuid):
        for service in services:
            if(uuid == service["uuid"]):
                return service, 200
        return "service not found", 404

class Packages_list(Resource):
    def get(self):
        return packages

class Packages(Resource):
    def get(self, uuid):
        for package in packages:
            if(uuid == package["uuid"]):
                return package, 200
        return "package not found", 404

# Create routes
api.add_resource(Services_list, '/api/v3/services')
api.add_resource(Services, '/api/v3/services/<uuid>')
api.add_resource(Packages_list, '/api/v3/packages')
api.add_resource(Packages, '/api/v3/packages/<uuid>')

# Run the application
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=32002, debug=True)


