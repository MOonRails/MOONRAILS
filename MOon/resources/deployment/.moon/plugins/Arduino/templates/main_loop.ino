void loop()
{    
    if(stringComplete == true){
      String tmp = inputString;
      inputString = "";
      inputString.reserve(200);
      stringComplete = false;

//      Serial.println("Received: ");
//      Serial.println(tmp);
      Serial.flush();
      
      int cmd_id = (int) (tmp.charAt(0) - '0');
      String params = tmp.substring(tmp.indexOf(':')+1);

 //     Serial.print("CMD id:");Serial.println(cmd_id);
 //     Serial.print("Params:");Serial.println(params);
      Serial.flush();


      switch(cmd_id){
%CASE%
      }
%ON_LOOP%

    }
}

