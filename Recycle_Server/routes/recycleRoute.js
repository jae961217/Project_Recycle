var express = require('express');
var router = express.Router();
var KalmanFilter=require('kalmanjs');



var Mat = require('../models/materials');

router.post('/check',(req,res)=>{
   var kf=new KalmanFilter();
    console.log('하드웨에 에서 받은값',req.body);

    var checkMetal='checkMetal';
    var frequency='frequency';
    Mat.insertData(checkMetal,req.body.metal);
    Mat.insertData(frequency,kf.filter(req.body.sound));
});
router.get('/readDataAll', (req, res) =>{
   console.log('connect!');
   Mat.showData(res);
});

router.get('/sendvalue',(req,res)=>{
   Mat.extractData(res);
});
module.exports = router;