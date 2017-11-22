#include "TimerOne.h"   //needed by MOonRails
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <AccelStepper.h>

#define DIR_AZ 6 /*PIN for Azimuth Direction*/
#define STEP_AZ 5 /*PIN for Azimuth Steps*/
#define DIR_EL 10 /*PIN for Elevation Direction*/
#define STEP_EL 9 /*PIN for Elevation Steps*/

#define EN 8 /*PIN for Enable or Disable Stepper Motors*/

#define SPR 200 /*Step Per Revolution*/
#define RATIO 54 /*Gear ratio*/
#define T_DELAY 20000 /*Time to disable the motors in millisecond*/

#define HOME_AZ 4 /*Homing switch for Azimuth*/
#define HOME_EL 3 /*Homing switch for Elevation*/

#define MAX_AZ_ANGLE 365 /*Maximum Angle of Azimuth for homing scanning*/
#define MAX_EL_ANGLE 365 /*Maximum Angle of Elevation for homing scanning*/

#define MAX_SPEED 300
#define MAX_ACCELERATION 100

#define MIN_PULSE_WIDTH 20 /*in microsecond*/

#define DEFAULT_HOME_STATE HIGH /*Change to LOW according to Home sensor*/

#define HOME_DELAY 6000 /*Time for homing Decceleration in millisecond*/

/*Global Variables*/
unsigned long t_DIS = 0; /*time to disable the Motors*/
/*Define a stepper and the pins it will use*/
AccelStepper AZstepper(1, STEP_AZ, DIR_AZ);
AccelStepper ELstepper(1, STEP_EL, DIR_EL);

void on_setup()
{  
  /*Change these to suit your stepper if you want*/
  AZstepper.setMaxSpeed(MAX_SPEED);
  AZstepper.setAcceleration(MAX_ACCELERATION);
  /*Change these to suit your stepper if you want*/
  ELstepper.setMaxSpeed(MAX_SPEED);
  ELstepper.setAcceleration(MAX_ACCELERATION);
  /*Set minimum pulse width*/
  AZstepper.setMinPulseWidth(MIN_PULSE_WIDTH);
  ELstepper.setMinPulseWidth(MIN_PULSE_WIDTH);
  /*Enable Motors*/
  pinMode(EN, OUTPUT);
  digitalWrite(EN, LOW);
  /*Homing switch*/
  pinMode(HOME_AZ, INPUT_PULLUP);
  pinMode(HOME_EL, INPUT_PULLUP);
  /*Initial Homing*/
  //Homing(deg2step(-MAX_AZ_ANGLE), deg2step(-MAX_EL_ANGLE)); //Homing disabled because ESOC equipment has no homing hardware..
}


/*Define the steps*/
int AZstep = 0;       //this is now global, as it's set by MO when it's value is published
int ELstep = 0;       //this is now global, as it's set by MO when it's value is published
  
void on_loop()
{ 
  
  /*Time Check*/
  if (t_DIS == 0)
    t_DIS = millis();

  /*Disable Motors*/
  if (AZstep == AZstepper.currentPosition() && ELstep == ELstepper.currentPosition() && millis()-t_DIS > T_DELAY)
    digitalWrite(EN, HIGH);
  /*Enable Motors*/
  else
    digitalWrite(EN, LOW);
  
  /*Move the Azimuth & Elevation Motor*/
  stepper_move(AZstep, ELstep);
}

/*Homing Function*/
void Homing(int AZsteps, int ELsteps)
{
  int value_Home_AZ = DEFAULT_HOME_STATE;
  int value_Home_EL = DEFAULT_HOME_STATE;
  boolean isHome_AZ = false;
  boolean isHome_EL = false;
  
  AZstepper.moveTo(AZsteps);
  ELstepper.moveTo(ELsteps);
  
  while(isHome_AZ == false || isHome_EL == false)
  {
    value_Home_AZ = digitalRead(HOME_AZ);
    value_Home_EL = digitalRead(HOME_EL);
    /*Change to LOW according to Home sensor*/
    if (value_Home_AZ == DEFAULT_HOME_STATE)
    {
      AZstepper.moveTo(AZstepper.currentPosition());
      isHome_AZ = true;
    }   
    /*Change to LOW according to Home sensor*/
    if (value_Home_EL == DEFAULT_HOME_STATE)
    {
      ELstepper.moveTo(ELstepper.currentPosition());
      isHome_EL = true;
    }
    if (AZstepper.distanceToGo() == 0 && !isHome_AZ)
    {
      error(0);
      break;
    }
    if (ELstepper.distanceToGo() == 0 && !isHome_EL)
    {
      error(1);
      break;
    }
    AZstepper.run();
    ELstepper.run();
  }
  /*Delay to Deccelerate*/
  long time = millis();  
  while(millis() - time < HOME_DELAY)
  {  
    AZstepper.run();
    ELstepper.run();
  }
  /*Reset the steps*/
  AZstepper.setCurrentPosition(0);
  ELstepper.setCurrentPosition(0); 
}

/*Error Handling*/
void error(int num_error)
{
  switch (num_error)
  {
    /*Azimuth error*/
    case (0):
      while(1)
      {
        Serial.println("AL001");
        delay(100);
      }
    /*Elevation error*/
    case (1):
      while(1)
      {
        Serial.println("AL002");
        delay(100);
      }
    default:
      while(1)
      {
        Serial.println("AL000");
        delay(100);
      }
  }
}

/*Send pulses to stepper motor drivers*/
void stepper_move(int stepAz, int stepEl)
{
  AZstepper.moveTo(stepAz);
  ELstepper.moveTo(stepEl);
    
  AZstepper.run();
  ELstepper.run();
}

/*Convert degrees to steps*/
int deg2step(double deg)
{
  return(RATIO*SPR*deg/360);
}

/*Convert steps to degrees*/
double step2deg(int Step)
{
  return(360.00*Step/(SPR*RATIO));
}

/*Check if is argument in number*/
boolean isNumber(char *input)
{
  for (int i = 0; input[i] != '\0'; i++)
  {
    if (isalpha(input[i]))
      return false;
  }
   return true;
}


/*   ---------- Here the MOonRails-specific commands start ---------- */



// Sets the Azimuth (degrees)
void setAzimuth(float targetAngle) {
  AZstep = deg2step((double)targetAngle);
  t_DIS = millis();
}

// Sets the Elevation (degrees)
void setElevation(float targetAngle) {
  ELstep = deg2step((double)targetAngle);
  t_DIS = millis();
}

// Publish the actual angle of the Azimuth
float publishAzimuth() {
   return step2deg(AZstepper.currentPosition());
}

// Publish the actual angle of the Elevation
float publishElevation() {
  return step2deg(ELstepper.currentPosition());
}

// Publish the difference (in degrees) between Target and Actual position of the Azimuth motor
float publishDeltaAzimuth() {
  return step2deg(AZstepper.currentPosition() - AZstep);
}

// Publish the difference (in degrees) between Target and Actual position of the Elevation motor
float publishDeltaElevation() {
  return step2deg(ELstepper.currentPosition() - ELstep);
}