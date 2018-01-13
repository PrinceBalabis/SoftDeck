
// The supported Arduino modifiers
// From: https://www.arduino.cc/en/Reference/KeyboardModifiers
void decodeMessage(String message) {
  //Serial.println("Decoding message...");
  // Check if the message contains the command declaration at the start of the message

  if (message.startsWith(modifierPressCall)) { // Press command
    Serial.println("Press-and-release command");
    char tempCommand = modifierProcessing(message);
    Keyboard.write(tempCommand); // Perform the command
  } else if (message.startsWith(modifierPressHoldCall)) { // Press-hold command
    Serial.println("Press-and-hold command");
    char tempCommand = modifierProcessing(message);
    Keyboard.press(tempCommand); // Perform the command
  } else if (message.startsWith(modifierReleasePressHoldCall)) { // Release-all command
    Serial.println("Release-key command");
    char tempCommand = modifierProcessing(message);
    Keyboard.release(tempCommand); // Perform the command
  } else if (message.startsWith(modifierReleaseAllCall)) { // Release-all command
    Serial.println("Release-all-keys command");
    Keyboard.releaseAll();
  } else {
    Serial.println("Normal string-print command");
    Keyboard.print(message);
  }
}

char modifierProcessing(String messageCommmand) {
  // Cut out the message and convert the message to an int which is readable for keyboard function
  char tempCommand = (int)messageCommmand.substring(4).toInt();
  Serial.print("Raw command code: ");
  Serial.println("Performing keyboard command...");
  return tempCommand;
}


