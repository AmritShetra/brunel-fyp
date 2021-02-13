## brunel-fyp
Investigating the use of Machine Learning and persuasive design to encourage habits of recycling amongst young adults.

The repository contains an Android application developed using Android Studio, and a back-end Flask server developed in Python.

### Setup
* Clone the repository and `cd` into the `server` directory.
* Build the docker container:
```
docker-compose up --build
```
* Note down the server machine's IP address using `ifconfig`, and add this to `/android/app/src/main/assets/env`, like so:
```
SERVER_ADDRESS=127.0.0.1
```
* In order to prevent future changes to the `env` file, use this command:
```
 git update-index --skip-worktree android/app/src/main/assets/env
```
* Install the Android app on your phone or use the emulator within Android Studio itself.

### Usage
* To observe the database, you can enter the container and open up a shell instance:
```
docker exec -it api /bin/bash
flask shell 
```
* You can then run database queries, like below, where we look at all User instances in the database (followed by an example of filtering).
```
User.query.all()
user = User.query.filter_by(username='Amrit').one()
```
* A set of tests can be run in the container:
```
python tests.py
```

### Notes
* [dotenv-java](https://github.com/cdimascio/dotenv-java) used to load environment variables in Android Studio.
* [android-async-http](https://loopj.com/android-async-http/) used for API calls.
* [Flask-Testing](https://pythonhosted.org/Flask-Testing/) used for API tests.