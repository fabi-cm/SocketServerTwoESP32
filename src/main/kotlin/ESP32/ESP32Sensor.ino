#include <WiFi.h>
#include <HTTPClient.h>

const char* ssid = "NameWifi";
const char* password = "PasswordWifi";
const char* serverUrl = "http://TuIPv4:8080/sensor"; // Usa tu IP real

const int trigPin = 23;  // GPIO23
const int echoPin = 22;  // GPIO22

void setup() {
  Serial.begin(115200);

  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

  connectToWiFi();
}

void connectToWiFi() {
  Serial.println("Conectando a WiFi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nConectado a WiFi");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    float distance = measureDistance();
    sendToServer(distance);
  } else {
    Serial.println("WiFi desconectado, reconectando...");
    connectToWiFi();
  }

  delay(2000);
}

float measureDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  long duration = pulseIn(echoPin, HIGH);
  float distance = duration * 0.034 / 2;

  Serial.print("Distancia: ");
  Serial.print(distance);
  Serial.println(" cm");

  return distance;
}

void sendToServer(float distance) {
  HTTPClient http;

  if (http.begin(serverUrl)) {
    http.addHeader("Content-Type", "text/plain");
    int httpCode = http.POST(String(distance));

    if (httpCode > 0) {
      Serial.printf("Código HTTP: %d\n", httpCode);
      if (httpCode == HTTP_CODE_OK) {
        String payload = http.getString();
        Serial.print("Respuesta: ");
        Serial.println(payload);
      }
    } else {
      Serial.printf("Error en POST: %s\n", http.errorToString(httpCode).c_str());
    }

    http.end();
  } else {
    Serial.println("Error al conectar con el servidor");
  }
}