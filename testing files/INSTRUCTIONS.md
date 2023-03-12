# Camera Testing Instructions
To configure the app for testing (camera functionality):
* create a new device in the emulator

* in the advanced settings of "virtual device configuration", ensure the front camera is set to "emulated" and the back camera is "virtual scene"

* launch the device, and in the settings (3 dots in right corner of the emulator toolbar)
select 'Camera'; place the test QR code (BFG5DGW54.png) image as the "wall" image

* click the add QR button, navigate to the back of the room using 
shift + WASD and shift + mouse

* position the scanner so that the test QR code is scanned

* once the QR code has been scanned, you can abort the app; the phone
will retain that position when the app is launched again

* run the camerafragment tests
