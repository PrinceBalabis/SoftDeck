/*-------------------------------------------------------------

  For best integration with Android phone, stick an NFC tag on the SoftDeck adapter,
  and make Tasker connect to bluetooth, launch the Android app and set display to never shut down.

  Simple receiving/sending between bluetooth-connected deviced and Arduino serial monitor
  If you use the Tinysine bluetooth module HM-10, then use their app for testing to
  connect and send/receive data, which you can get
  From their HM-10 product page. You can also download the source-code for their app
  To make your own version for custom functions.


  Setting up Bluetooth configuration
  - Connect Blueooth module to Serial-to-USB adapter
  - Open Arduino Serial Monitor
  - Set to "No line ending" & 9600 baud(9600 baud is default but try 38400 or 115200 if that didnt work)
  - Send "AT" twice to test if AT commands work

  Setup TinySine bluetooth module with the Windows program which can be downloaded on their site: https://www.tinyosshop.com/bluetooth-ble-module-ibeacon
  Press Factory Set and it should work out of the box

  Connect Bluetooth module to Arduino
  Connect the bluetooth module RX pin to Arduino TX pin
  Connect the bluetooth module TX pin to Arduino RX pin

  Default bluetooth password is 000000


  -----------------------
  Problems and solutions:

  Easily create new macros, and fast.
  All new macros shold be created on the Android app.
  No Arduino tweaking should be necessary.
  This means implementing all available keyboard modifiers and executing them via custom messages
  received from the Android client.
  Also features like delay, releaseall etc must be implemented for full micro customization
  capabilities from the Android app.

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
