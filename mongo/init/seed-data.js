db = db.getSiblingDB('object_compare');

db.objects.deleteMany({});

const objectA = {
  _id: ObjectId('64d1f3d9d9b0ea002f000001'),
  documentType: 'objectA',
  serviceCharacteristic: [
    { name: 'Home-ID', valueType: 'string7', value: { '@type': 'string7', value: '5F8RGZQ' } },
    { name: 'Technology', valueType: 'string7', value: { '@type': 'string7', value: 'FIBER' } },
    { name: 'State', valueType: 'string7', value: { '@type': 'string7', value: 'ACTIVE' } }
  ],
  updatedAt: new Date()
};

const objectBMatch = {
  _id: ObjectId('64d1f3d9d9b0ea002f000002'),
  documentType: 'objectB',
  serviceCharacteristic: [
    { name: 'Home-ID', valueType: 'string7', value: { '@type': 'string7', value: '5F8RGZQ' } },
    { name: 'Technology', valueType: 'string7', value: { '@type': 'string7', value: 'FIBER' } },
    { name: 'State', valueType: 'string7', value: { '@type': 'string7', value: 'ACTIVE' } }
  ],
  updatedAt: new Date()
};

const objectBMismatch = {
  _id: ObjectId('64d1f3d9d9b0ea002f000003'),
  documentType: 'objectB',
  serviceCharacteristic: [
    { name: 'Home-ID', valueType: 'string7', value: { '@type': 'string7', value: '5F8RGZQ' } },
    { name: 'Technology', valueType: 'string7', value: { '@type': 'string7', value: 'DSL' } },
    { name: 'State', valueType: 'string7', value: { '@type': 'string7', value: 'ACTIVE' } }
  ],
  updatedAt: new Date()
};

db.objects.insertMany([objectA, objectBMatch, objectBMismatch]);
