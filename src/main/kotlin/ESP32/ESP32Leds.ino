#include <WiFi.h>
#include <HTTPClient.h>

const char* ssid = "NameWifi";
const char* password = "PasswordWifi";
const char* serverUrl = "http://TuIPv4:8080/actuator";

const int redLed = 17;
const int yellowLed = 16;
const int greenLed = 4;

void setup() {
  Serial.begin(230400);

  pinMode(redLed, OUTPUT);
  pinMode(yellowLed, OUTPUT);
  pinMode(greenLed, OUTPUT);

  digitalWrite(redLed, LOW);
  digitalWrite(yellowLed, LOW);
  digitalWrite(greenLed, LOW);

  connectToWiFi();
  testLeds();
}

void testLeds() {
  digitalWrite(redLed, HIGH);
  delay(200);
  digitalWrite(redLed, LOW);
  digitalWrite(yellowLed, HIGH);
  delay(200);
  digitalWrite(yellowLed, LOW);
  digitalWrite(greenLed, HIGH);
  delay(200);
  digitalWrite(greenLed, LOW);
}

void connectToWiFi() {
  Serial.println("Conectando a WiFi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nConectado a WiFi");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    checkForCommands();
  } else {
    Serial.println("Reconectando WiFi...");
    connectToWiFi();
  }
  delay(500);
}

void checkForCommands() {
  HTTPClient http;

  if (http.begin(serverUrl)) {
    int httpCode = http.GET();

    if (httpCode == HTTP_CODE_OK) {
      String payload = http.getString();
      payload.trim();

      Serial.print("Comando recibido: ");
      Serial.println(payload);

      processCommand(payload);
    } else {
      Serial.print("Error en GET: ");
      Serial.println(httpCode);
    }

    http.end();
  } else {
    Serial.println("Error al conectar con el servidor");
  }
}

void processCommand(String command) {
  // Versión más robusta de procesamiento
  int redState = command.indexOf("tled:1") != -1 ? HIGH : LOW;
  int yellowState = command.indexOf("yled:1") != -1 ? HIGH : LOW;
  int greenState = command.indexOf("gled:1") != -1 ? HIGH : LOW;

  digitalWrite(redLed, redState);
  digitalWrite(yellowLed, yellowState);
  digitalWrite(greenLed, greenState);

  Serial.print("Estado LEDs - R:");
  Serial.print(redState ? "ON" : "OFF");
  Serial.print(" Y:");
  Serial.print(yellowState ? "ON" : "OFF");
  Serial.print(" G:");
  Serial.println(greenState ? "ON" : "OFF");
}