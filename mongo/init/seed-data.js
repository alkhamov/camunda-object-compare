db = db.getSiblingDB('object_compare');

db.objects.deleteMany({});

const objectA = {
  _id: ObjectId('64d1f3d9d9b0ea002f000001'),
  documentType: 'objectA',
  serviceCharacteristic: [
    { name: 'characteristicA', valueType: 'string7', value: { '@type': 'string7', value: 'valueA' } },
    { name: 'characteristicB', valueType: 'string7', value: { '@type': 'string7', value: 'valueB' } },
    { name: 'characteristicC', valueType: 'string7', value: { '@type': 'string7', value: 'valueC' } }
  ],
  updatedAt: new Date()
};

const objectBMatch = {
  _id: ObjectId('64d1f3d9d9b0ea002f000002'),
  documentType: 'objectB',
  serviceCharacteristic: [
    { name: 'characteristicA', valueType: 'string7', value: { '@type': 'string7', value: 'valueA' } },
    { name: 'characteristicB', valueType: 'string7', value: { '@type': 'string7', value: 'valueB' } },
    { name: 'characteristicC', valueType: 'string7', value: { '@type': 'string7', value: 'valueC' } }
  ],
  updatedAt: new Date()
};

const objectBMismatch = {
  _id: ObjectId('64d1f3d9d9b0ea002f000003'),
  documentType: 'objectB',
  serviceCharacteristic: [
    { name: 'characteristicA', valueType: 'string7', value: { '@type': 'string7', value: 'valueA' } },
    { name: 'characteristicB', valueType: 'string7', value: { '@type': 'string7', value: 'DIFFERENT' } },
    { name: 'characteristicC', valueType: 'string7', value: { '@type': 'string7', value: 'valueC' } }
  ],
  updatedAt: new Date()
};

db.objects.insertMany([objectA, objectBMatch, objectBMismatch]);
