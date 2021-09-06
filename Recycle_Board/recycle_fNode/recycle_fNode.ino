/*NodeMCU의 WiFi통신을 위한 헤더파일들*/
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h> //get한 String을 json으로
#include <SoftwareSerial.h>

#define nodeRX D3
#define nodeTX D1
#define inputRX D5 
#define inputTX D6
#define firstRX D7
#define firstTX D8

/*와이파이 연결을 위한 id와 password 배열*/
const char* WIFI_SSID = "AndroidHotspot8549";
const char* WIFI_PASS = "72811111";
const char* m_char = "";
int mCheck = -1;
int sCheck = 200;

WiFiClient wifiClient;    //와이파이 연결을 위한 객체선언
SoftwareSerial recvInputSerial(inputRX, inputTX);
SoftwareSerial recvFirstSerial(firstRX, firstTX);
SoftwareSerial sendNodeSerial(nodeRX, nodeTX);
HTTPClient http;    //HTTPClient 객체 선언

void connect()
{
  Serial.print("Connecting to ");
  Serial.println(WIFI_SSID);

  WiFi.mode(WIFI_OFF);
  delay(1000);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  /*연결 실패 시 5초 동안 기다리는 코드*/
  unsigned long wifiConnectStart = millis();
  while (WiFi.status() != WL_CONNECTED)
  {
    if (WiFi.status() == WL_CONNECT_FAILED)
    {
      Serial.println("Failed to connect to WIFI. Please verify credentials");
    }
    delay(500);
    Serial.println("...");
    // Only try for 5 seconds.
    if (millis() - wifiConnectStart > 10000)
    {
      Serial.println("Failed to connect to WiFi");
      Serial.println("Please attempt to send updated configuration parameters.");
      return;
    }
  }
  Serial.println();
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println();
}

void report(int metal, int sound)
{
  http.begin("http://13.124.99.108:3000/recycle/check"); //Server IP
  http.addHeader("Content-Type", "application/json");
  http.POST("{\"metal\":\"" + (String)metal + "\",\"sound\":\"" + (String)sound + "\"}");

  http.begin("http://13.124.99.108:3000/recycle/sendvalue"); //Server IP
  int get_size = http.GET();
  Serial.println(get_size);
  if (get_size > 0)
  {
    StaticJsonDocument<500> JSONBuffer;
    Serial.print("Get from Server : ");
    String response = http.getString();
    auto error = deserializeJson(JSONBuffer, response);
    const char * object = JSONBuffer["material"];
    String str1(object);
    Serial.println(object);
    
    if (str1.equals("Metal"))
      sendNode(0);
    else if (str1.equals("trash"))
      sendNode(1);
    else if (str1.equals("glass"))
      sendNode(2);
    else if (str1.equals("plastic"))
      sendNode(3);
  }
  else {
    Serial.println("Error on HTTP request");
  }

  http.end();  //Close connection
}

int receiveInput()
{
  if (recvInputSerial.available() > 0)
  {
    mCheck = recvInputSerial.read();
    if (mCheck == 0)
      mCheck = 137;
    else if (mCheck == 1)
      mCheck = 1023;
  }
  Serial.print("Metal Check : ");
  Serial.println(mCheck);

  return mCheck;
}

int receiveFirst()
{
  if (recvFirstSerial.available() > 0)
    sCheck = recvFirstSerial.read();
  Serial.print("Sound Check : ");
  Serial.println(sCheck);
  return sCheck;
}

void sendNode(int i)
{
  Serial.print("sendData : ");
  Serial.println(i);
  byte buf[1] = {};
  buf[0] = (byte)i;
  sendNodeSerial.write(buf, 1);
}


void setup() {
  Serial.begin(9600);
  recvInputSerial.begin(9600);
  recvFirstSerial.begin(9600);
  sendNodeSerial.begin(9600);
}


void loop() {
  /*만약 연결이 되지 않았다면 connect 함수를 호출하여 5초동안 지속적으로 연결을 시도한다.*/
  bool toReconnect = false;
  if (WiFi.status() != WL_CONNECTED)
  {
    Serial.println("Disconnected from WiFi");
    toReconnect = true;
  }
  if (toReconnect) connect();

  int metalV = receiveInput();
  int soundV = receiveFirst();

  if (metalV != -1 && soundV != 200)
  {
    report(metalV, soundV);
    mCheck = -1;
    sCheck = 200;
  }
}
