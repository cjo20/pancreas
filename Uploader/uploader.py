from pymongo import MongoClient
import pymongo
from pymongo.son_manipulator import SONManipulator

from enum import Enum
import getpass
import time
import random 

class Treatment:
	def __init__(self, amount, admin_time=None):
		self.amount = amount
		self.time = None
		if admin_time is None:
			self.time = time.time()
		else:
			self.time = admin_time

	def __str__(self):
		return ("%.2f" % self.amount) + " units at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime(self.time))

def encode_treatment(treatment):
	return {"_type": "Treatment", "amount": treatment.amount, "time": treatment.time}

def decode_treatment(document):
	assert document["_type"] == "Treatment"
	return Treatment(document["amount"], document["time"])

class Transform(SONManipulator):
	def transform_incoming(self, son, collection):
		for (key, value) in son.items():
			if isinstance(value, Treatment):
				son[key] = encode_treatment(value)
			elif isinstance(value, dict):
				son[key] = transform_incoming(value, collection)
		return son


	def transform_outgoing(self, son, collection):
		for (key, value) in son.items():
			if isinstance(value, dict):
				if "_type" in value and value["_type"] == "Treatment":
					son[key] = decode_treatment(value)
				else:
					son[key] = self.transform_outgoing(value, collection)
		return son


print('DIY Artificial Pancreas data uploader')
password = getpass.getpass(prompt="Please enter mongoDB password:")

print('Establishing connection to mongoDB\n')
try:
	client = MongoClient('mongodb://cjo20:' + password + '@ds041157.mongolab.com:41157/cjo20db')
except pymongo.errors.PyMongoError as e:
	print(e)
	exit(1)

print('Successfully logged in')
db = client['cjo20db']
collection = db['pancreas']

print('Got collection name:', collection.full_name, "\n")

db.add_son_manipulator(Transform())

print('Clearing collection data...')
collection.remove()

print('... and creating treatments')
now = time.time()

for x in range(0, 5):
	treatment_time = random.uniform(now, now - 60*60*24)
	treatment_time = (treatment_time // 60) * 60
	treatment = Treatment(random.uniform(0.1, 5), treatment_time)
	print(treatment)
	collection.insert({"Treatment": treatment})

#Add one within the last 3 hours
treatment_time = random.uniform(now - 100 * 60, now - 3*60*60)
treatment_time = (treatment_time // 60) * 60
treatment = Treatment(random.uniform(0.1, 5), treatment_time)
collection.insert({"Treatment": treatment})
print("inserting", treatment)

print('\nInserted, now trying to get them back...')
holder = db['pancreas'].find()
holder.sort("Treatment.time", pymongo.ASCENDING)
for result_object in holder:
	print(result_object["Treatment"])


