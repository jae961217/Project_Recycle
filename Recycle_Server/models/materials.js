var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var materialSchema = new Schema({
  sensor: String,
  value: Number,
}, { versionKey: false });

var Nor = require('./numberOfRecycleds');
var Mat = module.exports = mongoose.model('sensor', materialSchema);
module.exports.showData = function (sendor) {
  Mat.find((err, data) => {
    console.log(data);
    if (err) return sendor.status(500).send({ err: 'database failure' });
    sendor.json(data);
  });
};
module.exports.insertData = function (sensor, val) {
  var query = { sensor: sensor };
  var operator = { sensor: sensor, value: val };
  var option = { upsert: true };
  Mat.replaceOne(query, operator, option, function (err, upserted) {
    if (err) {
      console.log(err);
    }
    else {
    }
  });
};
module.exports.extractData = function (res) {
  var resultMaterial;
  Mat.find((err, data) => {
    if (err) return res.status(500).send({ err: 'database failure' });


    if (data[0].value < 1023) {
      resultMaterial = "Metal"
    }
    else if (data[1].value == 10) {
      resultMaterial = "trash";
    }
    else if (data[1].value < 100 && data[1].value > 10) {
      resultMaterial = "plastic";
    }
    else if (data[1].value >= 100) {
      resultMaterial = "glass";
    }

    var s = {
      material: `${resultMaterial}`
    };
    Nor.findOne({ type: `${resultMaterial}` }, (err, value) => {
      var tmp = value.number+1;
      var mx=value.max;
      //console.log(tmp);
      var query = { type: `${resultMaterial}` };
      var operator = {  type: `${resultMaterial}`, number:tmp,max:mx};
      var option = { upsert: true };
      Nor.replaceOne(query, operator, option, function (err, upserted) {
        if (err) {
          console.log(err);
        }
        else {
        }
      });

    
    });

    console.log('전달하는 내용', resultMaterial);
    res.json(s);
  });
};