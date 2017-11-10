#include "TimerOne.h"

//#include <stdio.h>
//#include <stdlib.h>

#define TEST_PIN  LED_BUILTIN

#define PIN_R  3
#define PIN_G  5
#define PIN_B  6

//a color..
struct color{
	int r;
	int g;
	int b;
};

void on_setup() {
	pinMode(TEST_PIN, OUTPUT);
	pinMode(PIN_R, OUTPUT);
	pinMode(PIN_G, OUTPUT);
	pinMode(PIN_B, OUTPUT);

}

//asas
void setLEDOff() {
	digitalWrite(TEST_PIN, false);
}

void setR(bool state) {
	digitalWrite(PIN_R, state? HIGH : LOW);
}

void setG(bool state) {
	digitalWrite(PIN_G, state? HIGH : LOW);
}
void setB(bool state) {

	digitalWrite(PIN_B, state? HIGH : LOW);
}


void setRGB(color c){
	analogWrite(PIN_R, c.r);
	analogWrite(PIN_G, c.g);
	analogWrite(PIN_B, c.b);
}

//This will set the state of the LED to On (true) or Off(false)
void setLEDStateA(bool state) {
	digitalWrite(TEST_PIN, state ? 1 : 0);
}


//this will publish the value of the resistor
float publishResistorValue() {
	  int analogPin = 0;
	  int raw = 0;
	  float Voutfraction;

	  raw = analogRead(analogPin);

	  if (raw) {
	    Voutfraction = (raw) / 1023.0;
	    return 100000.f * Voutfraction;
	  }
	  return 0;
}

