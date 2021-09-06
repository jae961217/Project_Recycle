var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var numOfRecSchema = new Schema({
  type: String,
  number: Number,
  max: Number
}, { versionKey: false });


var Nor = module.exports = mongoose.model('item',numOfRecSchema);
// module.exports.getData=function(dd){
//     Temp.find((err,data)=>{
//         if(err)return dd.status(500).send({err:'database failure'});
//         dd.json(data);
//     });
// }
module.exports.setMax = function (sensor, val) {
  Nor.findOne({ type: `${sensor}` }, (err, value) => {
    var tmp = value.number;
    var mx=val;
    //console.log(tmp);
    var query = { type: `${sensor}` };
    var operator = {  type: `${sensor}`, number:`${tmp}`,max:`${mx}`};
    var option = { upsert: true };
    Nor.replaceOne(query, operator, option, function (err, upserted) {
      if (err) {
        console.log(err);
      }
    });
  });
};
module.exports.showData = function (sendor) {
  Nor.find((err, data) => {
    console.log(data[0]);
    if (err) return sendor.status(500).send({ err: 'database failure' });
    sendor.json(data);
  });
};
module.exports.modifyData=function(name){
    Nor.findOne({ type: `${name}` }, (err, value) => {
        var tmp = 0;
        var mx=value.max;
        //console.log(tmp);
        var query = { type: `${name}` };
        var operator = {  type: `${name}`, number:tmp,max:mx};
        var option = { upsert: true };
        Nor.replaceOne(query, operator, option, function (err, upserted) {
          if (err) {
            console.log(err);
          }
        });
  
      
      });
}

// User.findOne({email:request.body.email,password:request.body.password},(err,users)=>{
//     if(err)return response.status(500).send({error: 'database failure'});
//     if(!users)return response.send('이메일 또는 패스워드가 일치하지않습니다');
//     response.send('로그인 성공!'+users.email);
// });
module.exports.extractData = function (res) {
 
  Nor.find( (err, data) => {
    if (err) return res.status(500).send({ err: 'database failure' });
    var s = { entranceM: `${ entUltramove}`,
    firstM:`${fUltramove}`,
    secondM:`${sUltramove}`,
    thirdM:`${sound}` 
    };
    console.log('send!');
    res.json(s);
  });
};