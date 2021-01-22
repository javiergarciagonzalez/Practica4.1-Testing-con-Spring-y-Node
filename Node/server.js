const app = require('./src/app');
const AWS= require('aws-sdk');
const port = process.env.PORT || 3000;
const createTableIfNotExist = require("./db/createTable")

// CONFIGURE AWS TO USE LOCAL REGION AND DEFAULT ENDPOINT (LOCALHOST) FOR DYNAMODB
AWS.config.update({
  region: process.env.AWS_REGION || 'local',
  endpoint: process.env.AWS_DYNAMO_ENDPOINT || 'http://localhost:8000'
});

// CREATE DYNAMODB TABLE ONLY IF NOT EXIST PREVIOUSLY
(async() => { 
  await createTableIfNotExist("films");
})();

app.listen(port, () => {
  console.log('Express server listening on port ' + port);
});