
//Generated
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete


void register_pubsubs(long frequency)
{
  if(frequency < 100000)
	  frequency = 100000;

  Timer1.initialize(frequency);         // initialize timer1, 500k is 1/2 second period
  Timer1.attachInterrupt(callback_pubsubs);  // attaches callback() as a timer overflow interrupt
}


void setup(){
	on_setup();

	register_pubsubs(100000);

	Serial.begin(115200);	

    inputString.reserve(200);

	//setup_setLEDState does not exist
	//setup_publishResitorValue();
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    
    Serial.print("STR::");Serial.println(inputString);	
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
//      Serial.println("New line");
      stringComplete = true;
    }
  }
}
