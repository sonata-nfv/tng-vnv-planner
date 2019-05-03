# Catalogue Service

from flask import Flask, jsonify, request
from flask_restful import abort, Api, fields, marshal_with, reqparse, Resource
from datetime import datetime
from models import TestPlanModel
import status
import logging

logging.basicConfig(level=logging.DEBUG)

class TestPlanManager():
    last_id = 0 ## it's a class attribute, and initially this is set to 0

    def __init__(self):
        self.test_plans = {}

    def insert_test_plan(self, test_plan):
        self.__class__.last_id += 1
        test_plan.id=self.__class__.last_id
        test_plan.status='STARTING'
        self.test_plans[self.__class__.last_id] = test_plan
        return test_plan

    def get_test_plan(self, uuid):
        return self.test_plans[id]

    def delete_test_plan(self, id):
        """This method receives the id of the test_plan that has to be removed from the self.test_plans dictionary"""
        del self.test_plans[id]

## Now, create a dictionary object with the args of the constructor of TestPlan Model,
## and, we need to declare the data types of each arg

test_plan_fields = {
    'id': fields.Integer,
    'test_plan_uuid': fields.String,
    'status': fields.String
}
## Now, create an object of Message Manager class
test_plan_manager = TestPlanManager()

class TestPlan(Resource):
    def abort_if_test_plan_doesnt_exist(self, id):
        if id not in test_plan_manager.test_plans:
            abort(status.HTTP_404_NOT_FOUND,
                 test_plan = "There is no testPlan with test_plan_uuid: {0}".format(id)
            )

    @marshal_with(test_plan_fields)
    def get(self, id):
        self.abort_if_test_plan_doesnt_exist(id)
        return test_plan_manager.get_test_plan(id)



    def delete(self, id):
        self.abort_if_test_plan_doesnt_exist(id)
        logging.debug('#$$#: vnvlog-curator: This is a debug message for entering to delete')
        logging.info('This is an info message')
        logging.warning('This is a warning message')
        logging.error('This is an error message')
        logging.critical('This is a critical message')
        test_plan_manager.delete_test_plan(id)
        return '', status.HTTP_204_NO_CONTENT


class TestPlanList(Resource):
    @marshal_with(test_plan_fields)
    def get(self):
        logging.info('#$$#: vnvlog-curator: This is a debug message for entering to get all test_plans through test-preparations GET')
        return [v for v in test_plan_manager.test_plans.values()]

    @marshal_with(test_plan_fields)
    def post(self):
        logging.debug('#$$#: vnvlog-curator: This is a debug message for entering to test-preparations POST')

        parser = reqparse.RequestParser()

        parser.add_argument('test_plan_uuid',
            type=str,
            required=True,
            help='test_plan_uuid cannot be blank!'
            )


        args = parser.parse_args()

        logging.debug('#$$#: vnvlog-curator - STEP1 - test_plan_uuid: %s',args['test_plan_uuid'])
        test_plan = TestPlanModel(test_plan_uuid=args['test_plan_uuid']
       )

        logging.debug('#$$#: vnvlog-curator - STEP2 - test_plan_uuid: %s',test_plan.test_plan_uuid)

        logging.debug('This is a debug message')
        logging.info('This is an info message')
        logging.warning('This is a warning message')
        logging.error('This is an error message')
        logging.critical('This is a critical message 0')

        test_plan_response = {
            'test_plan_uuid':test_plan.test_plan_uuid,
            'status':'STARTING'
        }
        test_plan_manager.insert_test_plan(test_plan)

        logging.debug('#$$#: vnvlog-curator - response - test_plan_uuid: %s',test_plan_response)
        logging.debug('#$$#: vnvlog-curator - STEP100 - THE END - test_plan_uuid: %s',args['test_plan_uuid'])
        return test_plan_response, status.HTTP_201_CREATED


class CompleteTestPlan(Resource):
    @marshal_with(test_plan_fields)
    def get(self):
        logging.info('#$$#: vnvlog-curator: This is a debug message for the trigger of the completion status to Planner ')

        """
        res = requests.post('http://tng-vnv-planner:6100/api/add_message/1234', json={"mytext":"lalala"})
        if res.ok:
            print res.json()
        """

        return [v for v in test_plan_manager.test_plans.values()]


# Instantiate the app
app = Flask(__name__)
api = Api(app)
api.add_resource(TestPlanList, "/api/v1/test-preparations")
api.add_resource(TestPlan, "/api/v1/test-preparations/<int:id>",
    endpoint = "message_endpoint"
    )

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=6200, debug=True)


