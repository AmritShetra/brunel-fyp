## brunel-fyp
Investigating the use of Machine Learning and persuasive design to encourage habits of recycling amongst young adults.

This project was completed as part of the FYP module of BSc Computer Science at Brunel University London, supervised by [Professor Rob Macredie](https://www.brunel.ac.uk/people/robert-macredie).

* Android application named "Recycler"
* Flask API
* Python image classifier
* Docker containers

### Setup
* Clone the repository and run a script to install dependencies:
```
bash setup.sh
```
* `cd` into the `server` directory and build the docker container:
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

### Commands run within the container
* To observe the database, you can enter the container and open up a shell instance:
```
docker exec -it api /bin/bash
flask shell 
```
* You can then run database queries, like below, where we look at all User instances in the database (followed by an example of filtering).
```
User.query.all()
user = User.query.filter_by(username='Amrit').first()
```
* A set of tests can be run in the container:
```
python tests.py
```

### Notes
* [dotenv-java](https://github.com/cdimascio/dotenv-java) used to load environment variables in Android Studio.
* [android-async-http](https://loopj.com/android-async-http/) used for API calls.
* [Flask-JWT-Extended](https://flask-jwt-extended.readthedocs.io/en/stable/) used for API token-based authentication.
* [Flask-Testing](https://pythonhosted.org/Flask-Testing/) used for API tests.
* [Kaggle image dataset](https://www.kaggle.com/piaoya/plastic-recycling-codes) used when training the classifier.
