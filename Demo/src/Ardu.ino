#include "TimerOne.h"

//#include <stdio.h>
//#include <stdlib.h>

#define TEST_PIN  LED_BUILTIN

struct DataSet{
	int var1;
	float var2;
};

void on_setup() {
	pinMode(TEST_PIN, OUTPUT);
}

//asas
void setLEDOff() {
	digitalWrite(TEST_PIN, false);
}

//This will set the state of the LED to On (true) or Off(false)
void setLEDStateA(bool state) {
	digitalWrite(TEST_PIN, state ? 1 : 0);
}


//this will publish the value of the resistor
float publishResistorValue() {
	int analogPin = 0;
	int raw = 0;
	int Vin = 5;
	float Vout = 0;
	float R1 = 100000;
	float R2 = 0;
	float buffer = 0;
	raw = analogRead(analogPin);

	if (raw) {
		buffer = raw * Vin;
		Vout = (buffer) / 1024.0;
		buffer = (Vin / Vout) - 1;
		R2 = R1 * buffer;
	}
	return R2;
}

