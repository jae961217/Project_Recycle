const express = require('express');
const path = require('path');
const cors = require('cors');
const app = express();
var fs=require('fs');
var compression=require('compression')
var mongoose = require('mongoose');
var server=require('http').createServer(app);


//[OCNFIGURE MONGOOSE]
//CONNECT TO MONGODB SERVER
var db = mongoose.connection;
db.on('error', console.error);
db.once('open', function () {
  //CONNECTED TO MONGODB SERVER
  console.log("CONNECTED to mongod server");
});
mongoose.connect('mongodb://humba:1234@13.124.99.108:27017/recycle?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false',{useCreateIndex:true,useNewUrlParser:true,useUnifiedTopology:true});


app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));
app.use(compression());
//passport 초기화 및 express-session 설정v

var materialrouter=require('./routes/recycleRoute');
app.use('/recycle',materialrouter);

var countrouter=require('./routes/countRoute');
app.use('/android',countrouter);

app.get('/api/hello',(req,res)=>{
  res.send(req.user);
});
server.listen(3000,()=>{
  console.log("연결");
});
// app.listen(3000, () => {
//   console.log("Express connected!");
// });


var io=require('socket.io')(server);
io.on('connection',function(socket){//소켓에 연결되었을시에
  console.log('connect됨');

  var tmp={glass: 0,glassMax:0,metal:0,metalMax:0,plastic:0,plasticMax:0,trash:0,trashmax:0};


 socket.emit("SEND",tmp);//안드로이드 소켓에 데이터전송
  socket.on('disconnect',function(data){
    console.log('disconnect');
  });
  
  socket.on('SEND',function(data){//안드로이드에서 메시지를 받으면
    var Nor  = require('./models/numberOfRecycleds');
    Nor.find((err,rcvdata)=>{
      if (err) return res.status(500).send({ err: 'database failure' });
      data.glass=rcvdata[0].number;
      data.glassMax=rcvdata[0].max;
      data.metal=rcvdata[1].number;
      data.metalMax=rcvdata[1].max;
      data.plastic=rcvdata[3].number;
      data.plasticMax=rcvdata[3].max;
      data.trash=rcvdata[2].number;
      data.trashmax=rcvdata[2].max;
      socket.emit('SEND',data);// 안드로이드로 메시지 전송
    });
  })
})
module.exports = app;




