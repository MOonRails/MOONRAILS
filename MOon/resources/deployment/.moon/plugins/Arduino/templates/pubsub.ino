void callback_%OP%(){
	float val =  %OP%();
	Serial.print(id_%OP%);
	Serial.print(":");
	Serial.println(val);
}

void register_%OP%(long frequency)
{
  if(frequency < 100000)
	  frequency = 100000;

  Timer1.initialize(frequency);         // initialize timer1, 500k is 1/2 second period
  Timer1.attachInterrupt(callback_%OP%);  // attaches callback() as a timer overflow interrupt
}

