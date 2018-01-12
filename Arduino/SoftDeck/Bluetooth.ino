/**-------------------------------------------------------------
  CheckIfMessageAvailable()

  -------------------------------------------------------------
**/
void CheckIfMessageAvailable() {
  if (Serial1.available()) { // Received message from blueooth
    //Serial.println("Received message from bluetooth, starting to receive");
    readString += (char) Serial1.read(); // Save one byte from serial buffer
    currentMillis = millis(); // Update current time
    previousReceivedBufferedMessageMillis = millis(); // Update current time
    //Serial.println("Check if there are several characters and start receiving all of them");
    while (true) // A timeout function, which if it detects a new character then it continues reading, but if it reaches timeout it assumes there are no more messagse
    {
      currentMillis = millis(); // Update current time
      if (currentMillis - previousReceivedBufferedMessageMillis >= timeout) { // Timeout reached since last character, stop waiting for more
        //Serial.println("Timeout waiting for new characters, jumping out of while loop");
        break; // Break out of "while(true)"-loop
      }
      if (Serial1.available()) { // Check if there are any message available, then save
        //Serial.println("Saving next character");
        readString += (char) Serial1.read(); // Saves one byte from serial buffer
        previousReceivedBufferedMessageMillis = currentMillis; // Reset timeout start-point
      }
      //delay(1); // Some delay in between checks
      //Serial.println("Repeating loop again..."); // Great way to check how long time between receiving each character
    } // End while (true)
    Serial.print("Raw message received: ");
    Serial.println(readString); // print the message from bluetooth
    decodeMessage(readString); // Decode message
    readString = ""; // clean the buffer string for a new message
    Serial1.print(1); // send a "1" back to show that the message has been received and processed!
    //Serial.println("Finished receiving message!");
  } // End if (Serial1.available()) {{
  //Serial.println("Looping..."); // Looping diagnostic message
}

