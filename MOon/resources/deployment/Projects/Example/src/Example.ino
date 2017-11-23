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

// Sets Red to full
void setRed(bool state) {
	digitalWrite(PIN_R, state? HIGH : LOW);
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

