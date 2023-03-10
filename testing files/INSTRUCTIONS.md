# Camera Testing Instructions
To configure the app for testing (camera functionality):
* create a new device in the emulator

* in the advanced settings of "virtual device configuration", ensure the front camera is set to "emulated" and the back camera is "virtual scene"

* launch the device, and in the settings (3 dots in right corner of the emulator toolbar)
select 'Camera'; select the test QR code (BFG5DGW54.png) image as the "wall" image

* click the add QR button and accept the system prompt to use the camera.

* navigate to the back of the room using shift + WASD and shift + mouse

* position the scanner so that the test QR code is scanned. **NOTE:** position the scanner slightly back [like this](https://i.imgur.com/ifkFrOv.png), so the QR code isn't too big in the view. This ensure the scanner can easily read the QR code, and won't stall during the tests. If it does stall during the test, adjust the scanner's position and reset the tests.

* once the code has been scanned, reject the prompt to take a photo

* accept the prompt to share location; kill the app process, and the device should remember the position of the QR scanner and permissions, so they will not have to be dealt with again during the tests

* run the camerafragment tests
