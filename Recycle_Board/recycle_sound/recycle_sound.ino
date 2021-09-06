#include <math.h>
#include <Wire.h>
#include <FHT.h> // include the library
#include <SoftwareSerial.h>

#define NO_RXD 10
#define NO_TXD 11
#define LOG_OUT 1 // use the log output function
#define FHT_N 256 // set to 256 point fht
SoftwareSerial sendInputArdu(NO_TXD, NO_RXD);

struct GyroKalman{
  float x_sound, x_bias;
  float P_00, P_01, P_10, P_11;
  float Q_sound, Q_gyro;
  float R_sound;
};

struct GyroKalman soundX;
struct GyroKalman soundY;
static const float Q_sound = 0.01;  //0.01 (Kalman)
static const float Q_gyro = 0.04; //0.04 (Kalman)
int trig = 4;
int echo = 5;
int data_num = 0;



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

void initGyroKalman(struct GyroKalman *kalman, const float Q_sound, const float R-sound) {
  kalman->Q_sound = Q_sound;
  kalman->R_sound = R_sound;
  
  kalman->P_00 = 0;
  kalman->P_01 = 0;
  kalman->P_10 = 0;
  kalman->P_11 = 0;
}

void predict(struct GyroKalman *kalman, float dot, float dt) {
  kalman->x_sound += dt * (dot - kalman->x_bias);
  kalman->P_00 += -1 * dt * (kalman->P_10 + kalman->P_01) + dt*dt * kalman->P_11 + kalman->Q_sound;
  kalman->P_01 += -1 * dt * kalman->P_11;
  kalman->P_10 += -1 * dt * kalman->P_11;
  kalman->P_11 += kalman->Q_gyro;
}

float update(struct GyroKalman *kalman, float sound_m) {
  const float y = sound_m - kalman->x_sound;
  const float S = kalman->P_00 + kalman->R_sound;
  const float K_0 = kalman->P_00 / S;
  const float K_1 = kalman->P_10 / S;
  kalman->x_sound += K_0 * y;
  kalman->x_bias += K_1 * y;
  kalman->P_00 -= K_0 * kalman->P_00;
  kalman->P_01 -= K_0 * kalman->P_01;
  kalman->P_10 -= K_1 * kalman->P_00;
  kalman->P_11 -= K_1 * kalman->P_01;
  return kalman->x_sound;
}

void sendToNode(int type)
{
  byte buf[1] = {};
  buf[0] = (byte)type;
  size_t z = sendInputArdu.write(buf, 1);
}




void setup() {
  Serial.begin(9600); // use the serial port
  sendInputArdu.begin(9600);

  

  pinMode(trig, OUTPUT);
  pinMode(echo, INPUT);

  //initGyroKalman(&angX, Q_sound, R_sound);
  TIMSK0 = 0; // turn off timer0 for lower jitter
  ADCSRA = 0xe5; // set the adc to free running mode
  ADMUX = 0x40; // use adc0
  DIDR0 = 0x01; // turn off the digital input for adc0
}

void loop() {
  int sumarr[800] = {0};
  int temp = 0;
  int temp_arr[10] = {0};
  int num = 0;
  int avg = 0;
  int isOrd = 0;
  int finish=0;
  //int isGP = 0; // 유리나 플라스틱으로 검사되면 1, 일쓰면 0


  for (temp = 0; temp < 800; temp++)
  {
    float distance = dist(trig, echo);

    //cli();  // UDRE interrupt slows this way down on arduino1.0
    for (int i = 0 ; i < FHT_N ; i++) { // save 256 samples
      while (!(ADCSRA & 0x10)); // wait for adc to be ready
      ADCSRA = 0xf5; // restart adc
      byte m = ADCL; // fetch adc data
      byte j = ADCH;
      int k = (j << 8) | m; // form into an int
      k -= 0x0200; // form into a signed int
      k <<= 6; // form into a 16b signed int
      fht_input[i] = k; // put real data into bins
    }
    fht_window(); // window the data for better frequency response
    fht_reorder(); // reorder the data before doing the fht
    fht_run(); // process the data in the fht
    fht_mag_log(); // take the output of the fht
    sei();
    //predict(&sndX,gx2,looptime);
    //gx1 = update(&sndX, accel_t_gyro.value.x_accel) / 10;
    
    int sum = 0;
    int cnt = 0;
    int cnt_ord = 0;
    int sum_ord = 0;

    for (int i = 64; i < FHT_N / 2; i++) {
      if (fht_log_out[i] > 80 && fht_log_out[i] < 180) {
        cnt += 1;
        sum += fht_log_out[i];
      }

      if (fht_log_out[i] < 80 && fht_log_out[i] > 70) {
        cnt_ord += 1;
        sum_ord += fht_log_out[i];
      }
    }

    /***************************플라스틱이나 유리 감별**********************/
    if (cnt != 0 )//&& isGP == 0) {
    {
      sumarr[temp] = sum / cnt;
      Serial.print("temp : ");
      Serial.println(temp);
      Serial.println(sumarr[temp]);

      temp_arr[num] = sumarr[temp];

      num += 1;

      data_num += 1;
    }

    if (num <= 8)
    {
      for (int j = 0; j < 8; j++) {
        avg += temp_arr[j];
      }
      avg /= 9;

      //isGP = 1;
    }
    /*********************************************************************/

    /************************일반쓰레기인지 감별***************************/
    if (cnt_ord != 0 && data_num == 0) {
      Serial.print("temp : ");
      Serial.println(temp);
      Serial.print("Ordinary? : ");
      Serial.println(sum_ord / cnt_ord);

      isOrd = 1;
    }
    /********************************************************************/

    if (temp == 799)//&& isGP == 1)
    {
      if (data_num == 0)
      {
        if (isOrd == 1)
        {
          Serial.println("10");
          Serial.println("Ordinary");
          sendToNode(10); // 일반쓰레기는 10을 보냄

          for (int i = 0; i < 8; i++)
          {
            temp_arr[i] = 0;
          }
          avg = 0;
          isOrd = 0;      
        }
        else
        {
          Serial.println("-1");
          sendToNode(200);

          for (int i = 0; i < 8; i++)
          {
            temp_arr[i] = 0;
          }
          avg = 0;
        }
      }
      else if (data_num >= 6) //유리
      {
        int result = data_num * 17;
        sendToNode(result); // 유리는 "data_num * 13" 를 보냄 -> 100보다 큰 숫자
        Serial.println(result);
        Serial.println("Glass");

        for (int i = 0; i < 8; i++)
        {
          temp_arr[i] = 0;
        }
        avg = 0;
      }
      else if (data_num > 0)
      {
        int result = 80 + (data_num - 1) * 3;
        sendToNode(result); // 플라스틱은 "(80 + ((data_num-1) *3))" 를 보냄 -> 80~98 사이의 숫자
        Serial.println(result);
        Serial.println("Plastic");

        for (int i = 0; i < 8; i++)
        {
          temp_arr[i] = 0;
        }
        avg = 0;
      }
    }
  }

  Serial.print("data_num : ");
  Serial.println(data_num);
  data_num = 0;
}
