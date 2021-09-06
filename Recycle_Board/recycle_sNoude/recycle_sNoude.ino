#include <SoftwareSerial.h>

#define secondRX D3
#define secondTX D1
#define nodeRX D5
#define nodeTX D6


int type=-1;

SoftwareSerial secondArdu(secondRX, secondTX);
SoftwareSerial recvNodeSerial(nodeRX, nodeTX);

void sendSecondArdu(int t)
{
  byte trashBuf[1] = {};
  trashBuf[0] = (byte)t;
  secondArdu.write(trashBuf, 1);
}

void receiveNode()
{
  if(recvNodeSerial.available()>0)
  {
    type=recvNodeSerial.read();
    Serial.println(type); 
  }
}

void setup() {
  Serial.begin(9600);
  secondArdu.begin(9600);
  recvNodeSerial.begin(9600);
}


void loop() {
  receiveNode();
  if(type!=-1)
  {
    Serial.println(type);
    sendSecondArdu(type);
    type=-1;
  }
}
