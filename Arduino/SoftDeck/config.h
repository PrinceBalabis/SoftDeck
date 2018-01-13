//-------------------------------------------------------------
// Basic configuration
//-------------------------------------------------------------
// The configured serial rate of the bluetooth module
const int bluetoothSerialRate = 9600;

// Serial rate between computer and arduino
const int arduinoSerialRate = 9600;

// The delay between serial commands
const int delayBetweenATCommands = 1000;

// Timeout. Time before giving up on waiting for new character in buffer (milliseconds). With OnePlus 5T+Tinysine+9600baud, timeout= 10ms seems to work fine(could maybe go lower)
const long timeout = 5;



/**-------------------------------------------------------------
  HID configuration
  Important that each command is exactly a 4 character unique string!
  -------------------------------------------------------------
**/

// Macro press and release command. String to put in front of each text which is wished to be declared a command and NOT a text
const String modifierPressCall = "d9D4";

// Macro press-hold command. String to put in front of each text which is wished to be declared a command and NOT a text
const String modifierPressHoldCall = "k4C3";

// Macro release-hold command. String to put in front of each text which is wished to be declared a command and NOT a text
const String modifierReleasePressHoldCall = "K0t4";

// Macro release-all-hold command.
const String modifierReleaseAllCall = "k7G8";
