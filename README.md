## brunel-fyp
Investigating the use of Machine Learning and persuasive design to encourage habits of recycling amongst young adults.

The repository contains an Android application developed using Android Studio, and a back-end Flask server developed in Python.

### Setup
* Clone the repository.
* `cd` into the `server` directory and install the requirements.
```
pip3 install -r requirements.txt
```
* Run the Flask server:
```
python3 api.py
```
* Note down the server machine's IP address, and add this to `/android/app/src/main/assets/env`, like so:
```
SERVER_ADDRESS=127.0.0.1
```
* In order to prevent future changes to the `env` file, use this command:
```
 git update-index --skip-worktree android/app/src/main/assets/env
```
* Install the Android app on your phone or use the emulator within Android Studio itself.
