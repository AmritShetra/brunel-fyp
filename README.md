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
* You can then run database queries, like below, where a new User instance is stored in the table. The first and last lines are used to illustrate the changes made.
```
User.query.all()
db.session.add(User(username="Amrit"))
db.session.commit()
User.query.all()
```

