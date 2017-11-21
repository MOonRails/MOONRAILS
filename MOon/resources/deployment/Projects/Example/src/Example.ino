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

void on_loop(){
	//does nothing for the moment
}

// Sets Red to full
void setRed(bool state) {
	digitalWrite(PIN_R, state? HIGH : LOW);
}

// Sets Green to full
void setGreen(bool state) {
	digitalWrite(PIN_G, state? HIGH : LOW);
}

// Sets Blue to full
void setBlue(bool state) {
	digitalWrite(PIN_B, state? HIGH : LOW);
}


// 0 to  255 value for all channels (RGB)
void setGray(unsigned char gradient) {
	analogWrite(PIN_R, gradient);
	analogWrite(PIN_G, gradient);
	analogWrite(PIN_B, gradient);
}


// set's any RGB (0 up to 255)
void setRGB(color c){
	analogWrite(PIN_R, c.r);
	analogWrite(PIN_G, c.g);
	analogWrite(PIN_B, c.b);
}

//This will set the state of the LED to On (true) or Off(false)
void setTestLED(bool state) {
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


