var express = require('express');
var router = express.Router();
var Nor  = require('../models/numberOfRecycleds');

router.post('/modify',(req,res)=>{// 안드로이드 버튼클릭시 값을 0으로 초기화한다.
    console.log('요청 들어옴',Object.keys(req.body)[0]);
    var tmp=Object.keys(req.body)[0];
    Nor.modifyData(tmp);//데이터베이스의 보관함의 현재용량을 0으로 초기화하는 함수
    var s={"결과": "성공"};
    res.json(s);
});


router.post('/getSettingMax',(req,res)=>{// 보관함 용량 값을 안드로이드에서 보내준값으로 초기화한다.
    var tmp=JSON.parse(Object.keys(req.body)[0]);
   Nor.setMax("glass",tmp.glass);//glass 보관함의 용량을 값을 해당값으로 변경
   Nor.setMax("Metal",tmp.Metal);
   Nor.setMax("plastic",tmp.plastic);
   Nor.setMax("trash",tmp.trash);
   var s={"결과": "성공"};
    res.json(s);
});
module.exports = router;