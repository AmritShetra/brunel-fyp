from flask import Flask, jsonify, request
from config import Config
from models import db, User, Trophies, MachineLearning

app = Flask(__name__)
app.config.from_object(Config)

# Specify which app we are using with SQLAlchemy
app.app_context().push()
db.init_app(app)


@app.route('/')
def home():
    response = {
        "text": "Hello world!"
    }
    return jsonify(response)


@app.route('/signup/', methods=['POST'])
def create_user():
    data = request.json

    # Check if the username/email are taken
    response = ""
    if User.query.filter_by(username=data["username"]).first():
        response += "Username already exists."
    if User.query.filter_by(email=data["email"]).first():
        response += "Email already exists."
    if not response == "":
        return response, 409

    # If not, add the user to the database
    user = User(
        username=data["username"],
        password=data["password"],
        email=data["email"],
        first_name=data["first_name"],
        last_name=data["last_name"]
    )
    db.session.add(user)

    # And create a trophies object with the new user's id
    user_id = User.query.filter_by(username=data["username"]).first().id
    trophies = Trophies(
        user_id=user_id
    )
    db.session.add(trophies)

    db.session.commit()
    return user.username + " added to database.", 200


@app.route('/login/', methods=['POST'])
def get_user():
    data = request.json

    # Checking if the username exists beforehand
    user = User.query.filter_by(username=data["username"]).first()
    if not user:
        return "Username not found.", 401

    if user.password == data['password']:
        return "Authenticated.", 200
    else:
        return "Please check your login details and try again.", 401


# Provides access to objects in shell without needing to import manually
@app.shell_context_processor
def make_shell_context():
    return {
        'db': db,
        'User': User,
        'Trophies': Trophies,
        'MachineLearning': MachineLearning
    }


# https://stackoverflow.com/questions/7023052/configure-flask-dev-server-to-be-visible-across-the-network
if __name__ == '__main__':
    # Create database tables
    db.create_all()
    app.run(host='0.0.0.0')
