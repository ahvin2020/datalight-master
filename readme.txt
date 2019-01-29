1. setting up:
- put DataLight project folder AND aFileChooser project folder in same directory
- in DataLight, set aFileChooser and project library (right click Data Light -> Android, then make sure aFileChooser is selected as Library)
- run DataLight

2. testing
- use 2 device (Sender and Receiver)
- Receiver device, click Receiver button, then point at Sender device screen, then hold (don't move)
- Sender device, click Sender button, choose file to send (as small as possible for now)
- When sending completes, Receive will popup success dialog with file name, file is saved to /sdcard/<file_name>



3. todo:
- use flash light to signal start sending data (done)
- add metadata to every packet (byte number)
- when receiving, use flashlight (2 blinks) to "reverse qr code"

- error checking
- synchronize sending and receiving using flashlight + front camera (acknowledge when start transmit, and also data received)
- use color qr code (in progress)
- reading entire file into app "might" cause out of memory
- use multithreading for sending and receiving

optional:
- live video streaming (for demo at hackathon)