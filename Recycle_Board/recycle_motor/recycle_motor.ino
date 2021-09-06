#include<Servo.h>
#include <SoftwareSerial.h>

#define NO_RXD 2
#define NO_TXD 3
SoftwareSerial recvInputArdu(NO_RXD, NO_TXD);

int servo_2nd = 8;
int servo_3rd = 9;
int servo_4th = 12;

int trig3 = 4;
int echo3 = 5;
int trig4 = 6;
int echo4 = 7;

int type = -1;

Servo s2;
Servo s3;
Servo s4;

void receiveD()
{
  if (recvInputArdu.available() > 0)
  {
    type = recvInputArdu.read();
    Serial.println(type);
  }
}

float dist(int trig, int echo) { //초음파센서로 거리측정 함수
  digitalWrite(trig, LOW);
  digitalWrite(echo, LOW);
  delayMicroseconds(2);
  digitalWrite(trig, HIGH);
  delayMicroseconds(10);
  digitalWrite(trig, LOW);

  unsigned long duration = pulseIn(echo, HIGH);
  float distance = ((float)(340 * duration) / 10000) / 2;

  return distance;
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  recvInputArdu.begin(9600);

  s2.attach(servo_2nd);
  s3.attach(servo_3rd);
  s4.attach(servo_4th);

  pinMode(trig3, OUTPUT);
  pinMode(echo3, INPUT);
  pinMode(trig4, OUTPUT);
  pinMode(echo4, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  //metal : 0, trash : 1, glass : 2, plastic : 3

  receiveD();

  //test
  if (type != -1)
  {
    if (type == 0) { // 금속 0인게 왼쪽
      s2.write(0);
      delay(1000);
      s2.write(90);
      delay(1000);

      type = -1;
    }

    if (type == 1)
    {
      s2.write(180);
      delay(1000);
      s2.write(90);
      delay(3000);

      if (dist(trig3, echo3) <= 15) // 일반 쓰레기
      {
        //s3을 일쓰로
        s3.write(0);
        delay(1000);
        s3.write(45);
        delay(1000);
      }

      type = -1;
    }

    else if (type == 2)
    {
      s2.write(180);
      delay(1000);
      s2.write(90);
      delay(3000);

      if (dist(trig3, echo3) <= 15)
      {
        //s3을 일쓰 반대로
        s3.write(180);
        delay(1000);
        s3.write(45);
        delay(3000);

        if (dist(trig4, echo4) <= 15)
        {
          //s4를 유리
          s4.write(180);
          delay(1000);
          s4.write(45);
          delay(1000);
        }
      }
      type = -1;
    }

    else if (type == 3)
    {
      s2.write(180);
      delay(1000);
      s2.write(90);
      delay(3000);

      if (dist(trig3, echo3) <= 15)
      {
        //s3을 일쓰 반대로
        s3.write(180);
        delay(1000);
        s3.write(45);
        delay(3000);

        if (dist(trig4, echo4) <= 15)
        {
          //s4를 플라스틱
          s4.write(0);
          delay(1000);
          s4.write(45);
          delay(1000);
        }
      }
      type = -1;
    }
  }
}
