//초음파 2개, 블루투스 1개, 금속감지 1개, 서보모터 2개(1개는 전력잡아먹기용)
#include<Servo.h>
#include <SoftwareSerial.h>

#define BT_RXD 2
#define BT_TXD 3
SoftwareSerial bluetooth(BT_RXD, BT_TXD);

#define NO_RXD 10
#define NO_TXD 11
SoftwareSerial sendInputArdu(NO_TXD, NO_RXD);

int servo_door = 6;
int servo_move = 9;
int trig1 = 4;
int echo1 = 5;
int trig2 = 12;
int echo2 = 13;
int Real_eat = 8;
#define metal_pin A0

int check = 0;
int isHand = 0;

Servo Door;
Servo moving;
Servo Real;

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

void sendToNode(int type)
{
  byte buf[1] = {};
  buf[0] = (byte)type;
  size_t z = sendInputArdu.write(buf, 1);
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  bluetooth.begin(9600);
  sendInputArdu.begin(9600);

  moving.attach(servo_move);
  Door.attach(servo_door);
  Real.attach(Real_eat);

  pinMode(trig1, OUTPUT);
  pinMode(echo1, INPUT);
  pinMode(trig2, OUTPUT);
  pinMode(echo2, INPUT);

  Door.write(90);
  Real.write(90);
}

void loop() {
  // put your main code here, to run repeatedly:
  check = 0;
  moving.write(100);
  //여기가 맨 첫단계
  if (check == 0) {
    float distance = dist(trig1, echo1);

    if (distance <= 15)
    { //손이나 물체가 있을 때
      Serial.println(distance);
      bluetooth.println("1");
    }
    else
    { //손이나 물체가 입구에서 안걸리고, 물체가 들어와 있는 상황일 때
      bluetooth.println("0");
      int state = 0;
      float distance2 = 0;
      for (int i = 0; i < 3; i++)
      {
        distance2 = dist(trig2, echo2);
        if (distance2 > 12)
        {
          Serial.println(distance2);
          state = 1;
          break;
        }
        delay(1000);
      }

      if (state == 0)
      {
        Serial.println(distance2);
        check = 1;
      }
    }
  }

  //여기가 두번째 단계
  if (check == 1)
  {
    Door.write(180);

    int isMetal = 0;
    int cnt = 0;
    while (cnt < 15) { //3초에 걸쳐 15번의 검사를 실행
      //cnt = cnt + 1;
      cnt++;
      int metal = analogRead(metal_pin); //센서로 금속값 읽어오기
      Serial.print("Metal : ");
      Serial.println(metal);

      if (metal < 600)
      { //중간에 금속으로 판단된 상황
        isMetal = 1;
        break;
      }
      delay(100);
    }
    // 금속감지센서

    if (isMetal == 1)
    { //금속인 경우
      Serial.println("Metal");
      sendToNode(0);
    }
    else
    { //금속이 아닌 경우
      Serial.println("Non-metal");
      sendToNode(1);
    }

    moving.write(10);
    delay(2000);
    moving.write(100);
    //물체를 모터 기준 오른쪽(2단계)로 떨어뜨리고 원래 각도로 복귀

    for (int i = 0; i < 8; i++)
    {
      delay(3000);
    }
    Door.write(90);
  }
  delay(1000);
}
