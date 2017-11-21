
//Generated
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete


void setup(){
	on_setup();
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
