/*-------------------------------------------------------------

  SoftDeck
  

  -------------------------------------------------------------
*/


//-------------------------------------------------------------
// Includes
//-------------------------------------------------------------
#include "Keyboard.h"
#include "config.h"

//-------------------------------------------------------------
// Logic
//-------------------------------------------------------------
String readString = "";
unsigned long currentMillis = 0; // To store current time
unsigned long previousReceivedBufferedMessageMillis = 0; // To store last time character was received from buffer

//-------------------------------------------------------------
// Setup
//-------------------------------------------------------------
void setup()
{
  delay(6000); // Give Bluetooth module some time to initialize

  Serial.begin(arduinoSerialRate); // Open serial communications
  while (!Serial);// FOR DEBUGGING ONLY. REMOVE FOR LIVE VERSION. Wait for serial port to connect. (Arduino will idle until serial communication is established)
  delay(500); // Let capacitors stabilize

  Serial.println("Welcome to Testing Bluetooth Sketch!");

  // Initialize Bluetooth
  initBluetooth();

  // Initialize HID emulation
  initHIDEmulation();

  Serial.println("Started Testing Mode for bluetooth...");
  Serial.println("Idle...");
}

//-------------------------------------------------------------
// Loop
//-------------------------------------------------------------
void loop()
{
  CheckIfMessageAvailable(); // Check if message available for bluetooth send/receive
}

//-------------------------------------------------------------
// initBluetooth
//-------------------------------------------------------------
void initBluetooth() {
  Serial.println("Initializing Bluetooth module...");
  delay(500); // Let capacitors stabilize
  Serial1.begin(bluetoothSerialRate); // set the data rate for the SoftwareSerial port connected to the bluetooth module
  delay(1000); // Let capacitors stabilize
  while (Serial1.available()) { // Throw out any start-messages
    Serial1.read(); // Dump serial
  }
  while (Serial.available()) { // Throw out any start-messages
    Serial.read(); // Dump serial
  }
  delay(500); // Let capacitors stabilize
} // End initBluetooth();

//-------------------------------------------------------------
// initHIDEmulation
//-------------------------------------------------------------
void initHIDEmulation() {
  Serial.println("List of modifier commands at: https://www.arduino.cc/en/Reference/KeyboardModifiers");
  Serial.println("Initializing HID emulation...");
  delay(500); // Let capacitors stabilize

  // initialize control over the keyboard:
  Keyboard.begin();
} // End initHIDEmulation();
