import werkzeug
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
    # Returns a list of users in the database.
    response = {
        "users": []
    }
    for user in User.query.order_by(User.id).all():
        user_data = {
            "id": user.id,
            "username": user.username
        }
        response["users"].append(user_data)
    return jsonify(response), 200


@app.route('/register/', methods=['POST'])
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
def login():
    data = request.json

    # Checking if the username exists beforehand
    user = User.query.filter_by(username=data["username"]).first()
    if not user:
        return "Username not found.", 401

    if user.password == data['password']:
        return "Authenticated.", 200
    else:
        return "Please check your login details and try again.", 401


@app.route('/users/', methods=['GET'])
def get_user():
    username = request.authorization.username
    password = request.authorization.password

    # Checking if the account exists
    user = User.query.filter_by(username=username).first()
    if not user:
        return "Username not found.", 401

    # If account is found but passwords don't match
    if not user.password == password:
        return "Not authenticated, please login again", 401

    # If passwords match, send the account's details over
    data = {
        "first_name": user.first_name,
        "last_name": user.last_name,
        "email": user.email
    }
    return data, 200


@app.route('/users/edit/', methods=['PUT'])
def update_user():
    username = request.authorization.username
    password = request.authorization.password
    data = request.json

    # Checking if the account exists
    user = User.query.filter_by(username=username).first()
    if not user:
        return "Username not found.", 401

    # If account is found but passwords don't match
    if not user.password == password:
        return "Not authenticated, please login again", 401

    # If username is being changed, check if the new one already exists
    if not user.username == data['username']:
        if User.query.filter_by(username=data['username']).first():
            return "Username is taken", 409

    # Same thing with email
    if not user.email == data['email']:
        if User.query.filter_by(email=data['email']).first():
            return "Email is taken", 409

    # Looks like everything is fine, update the fields
    user.username = data['username']
    user.password = data['password']
    user.email = data['email']
    user.first_name = data['first_name']
    user.last_name = data['last_name']

    db.session.commit()
    return "User model updated", 200


@app.route('/trophies/', methods=['GET'])
def get_trophies():
    username = request.authorization.username
    password = request.authorization.password

    # Checking if the account exists
    user = User.query.filter_by(username=username).first()
    if not user:
        return "Username not found.", 401

    # If account is found but passwords don't match
    if not user.password == password:
        return "Not authenticated, please login again", 401

    # If passwords match, send the account's trophies over
    trophies = Trophies.query.filter_by(user_id=user.id).first()

    data = {
        "trophy_one": trophies.trophy_one,
        "trophy_two": trophies.trophy_two
    }

    return data, 200


@app.route('/classify/', methods=['POST'])
def process_photo():
    photo = request.files["photo"]
    photo_filename = werkzeug.utils.secure_filename(photo.filename)
    photo.save(photo_filename)
    return "Photo received", 200


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
