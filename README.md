# SoftDeck

Features:
(Future)Absense lock. When adapter and phone gets at a certain distance from each other(either disconnected or using antenna strength signal), it will lock the computer(perform Win+L).


Requirements:
-Android device with bluetooth
-Arduino with USB HID(Pro Micro or Leonardo)
-Bluetooth module(preferably the TinySine HM-10 BLE 4.0)
-Serial-to-USB adapter
-Extra: Tasker-app, Android device with NFC reader, NFC tags

For best integration with Android phone, stick an NFC tag on the SoftDeck adapter,
 and make Tasker connect to bluetooth, launch the Android app and set display to never shut down.


Testing bluetooth module
- Connect Blueooth module to Serial-to-USB adapter
- Open Arduino Serial Monitor
- Set to "No line ending" & 9600 baud(9600 baud is default but try 38400 or 115200 if that didnt work)
- Send "AT" twice to test if AT commands work
  
Setup TinySine Bluetooth module with the Windows program which can be downloaded on their site: https://www.tinyosshop.com/bluetooth-ble-module-ibeacon
Press Factory Set and it should work fine for this project
  
Connect Bluetooth module to Arduino
Connect the bluetooth module RX pin to Arduino TX pin
Connect the bluetooth module TX pin to Arduino RX pin

Default bluetooth password for the HM-10 is 000000



  -----------------------
  Problems and solutions:

  Easily create new macros, and fast.
  All new macros should be created on the Android app.
  No Arduino tweaking should be necessary when adding new macros.
  This means implementing all available keyboard modifiers and executing them via custom messages
  received from the Android client.


How to use:

To send text(which will print out like a copy-paste on the computer), just send a normal string text from the app.
For example, sending hello will print out hello, or sending 1234 will print out 1234 etc.

There are different modifier macros available on the Arduino. 
To use a modifier, send the "modifier action code+decimal modifier key"
For example, if you want to "Hold Shift", then you need to send "k4C3133". The first 4 characters(k4C3) is the modifier to perform a "press-and-hold"-command.
The rest of the characters "133" is the decimal value for the right-shift-button(check the Arduino documentation)
Another example is to send "k0t4133", which will release the Shift key.
Another example is to send "k7G8", which will release ALL currently held keys
Another example is to send "d9D4131", which will press the Windows key(bring up the Start Menu)
Another example is to send "k4C3131"(Hold Win), then right after send the "d9D4l"(press-and-release l), and then send k7G8(release-all-keys) this will perform WIN+L, which will lock the computer

Macro action codes list:
Press once: "d9D4"
Press-and-hold "k4C3"
Release a key: "K0t4"
Release all keys(that are currently held): "k7G8"

List of modifier decimal keys at: 
https://www.arduino.cc/en/Reference/KeyboardModifiers


 