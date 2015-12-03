//
// You should run before:
// npm install express body-parser
// 
// node server.js
//
var app = require('express')();
var bodyParser = require('body-parser');

app.use(bodyParser.json())
  .post('/', function(req, res) {
    console.log(req.body);
    res.status(200).send('OK');
  })
  .listen(3000);
