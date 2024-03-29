import numpy as np
import tensorflow as tf
import werkzeug

from flask import Flask, request

from flask_jwt_extended import create_access_token
from flask_jwt_extended import get_jwt_identity
from flask_jwt_extended import jwt_required
from flask_jwt_extended import JWTManager

from classifier import color_mode, img_height, img_width
from config import Config
from models import db, User, Trophies
from utils import get_desc, labels

app = Flask(__name__)
app.config.from_object(Config)
jwt = JWTManager(app)

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

    return response, 200


@app.route('/register/', methods=['POST'])
def create_user():
    data = request.json

    # Check if the username/email are taken
    response = {}
    if User.query.filter_by(username=data["username"]).first():
        response['username'] = "Username already exists."
    if User.query.filter_by(email=data["email"]).first():
        response['email'] = "Email already exists."
    if response:
        return response, 409

    # If not, add the user to the database
    user = User(
        username=data["username"],
        email=data["email"],
        first_name=data["first_name"],
        last_name=data["last_name"]
    )
    user.set_password(data['password'])
    db.session.add(user)

    # And create a trophies object with the new user's id
    user_id = User.query.filter_by(username=data["username"]).first().id
    trophies = Trophies(
        user_id=user_id
    )
    db.session.add(trophies)

    # Save to the database
    db.session.commit()
    response['message'] = "{} added to database.".format(user.username)

    return response, 200


@app.route('/login/', methods=['POST'])
def login():
    username = request.json.get("username")
    password = request.json.get("password")

    response = {}

    user = User.query.filter_by(username=username).first()
    if not user:
        response['message'] = "Username not found."
        return response, 401

    if user.check_password(password):
        response['access_token'] = create_access_token(identity=user.id)
        return response
    else:
        response['message'] = "Please check your login details and try again."
        return response, 401


@app.route('/users/', methods=['GET'])
@jwt_required()
def get_user():
    current_user = get_jwt_identity()
    user = User.query.filter_by(id=current_user).first()
    response = {
        "username": user.username,
        "email": user.email,
        "first_name": user.first_name,
        "last_name": user.last_name
    }
    return response, 200


@app.route('/users/edit/', methods=['PUT'])
@jwt_required()
def update_user():
    current_user = get_jwt_identity()
    data = request.json

    response = {}

    user = User.query.filter_by(id=current_user).first()

    # If username is being changed, see if the new one already exists
    if not user.username == data['username']:
        if User.query.filter_by(username=data['username']).first():
            response['message'] = "Username is taken."
            return response, 409

    # Same thing with email
    if not user.email == data['email']:
        if User.query.filter_by(email=data['email']).first():
            response['message'] = "Email is taken."
            return response, 409

    user.username = data['username']
    user.email = data['email']
    user.first_name = data['first_name']
    user.last_name = data['last_name']

    db.session.commit()
    response['message'] = "Changes saved."
    return response, 200


@app.route('/trophies/', methods=['GET'])
@jwt_required()
def get_trophies():
    current_user = get_jwt_identity()
    trophies = Trophies.query.filter_by(user_id=current_user).first()
    response = {
        "trophy_one": trophies.trophy_one,
        "trophy_two": trophies.trophy_two
    }
    return response, 200


@app.route('/trophies/', methods=['PUT'])
@jwt_required()
def update_trophies():
    current_user = get_jwt_identity()
    response = {}

    data = request.json
    key = data['trophy_name']
    trophies = Trophies.query.filter_by(user_id=current_user).first()
    if getattr(trophies, key):
        response['message'] = "Trophy is already unlocked"
        return response, 304

    # Unlock the trophy
    setattr(trophies, key, True)
    db.session.commit()
    response['message'] = "You've unlocked an achievement - well done!"

    return response, 200


@app.route('/classify/', methods=['POST'])
def process_photo():
    photo = request.files['photo']
    photo_filename = werkzeug.utils.secure_filename(photo.filename)
    photo.save(photo_filename)

    img = tf.keras.preprocessing.image.load_img(
        photo_filename,
        color_mode=color_mode,
        target_size=(img_height, img_width)
    )

    # Turn it into an array of pixels
    img_array = tf.keras.preprocessing.image.img_to_array(img)
    # Create a batch/list to give to the model
    img_array = tf.expand_dims(img_array, 0)

    model = tf.keras.models.load_model('my_model')
    # An array of the model's confidence for each possible label
    predictions = model.predict(img_array)

    # Use the index of the highest confidence value
    label = labels[np.argmax(predictions)]
    # Get the highest confidence value in the array
    confidence = np.max(predictions) * 100

    sentence = (
        f"I've taken a quick look. \n"
        f"This image is {int(confidence)}% likely to be resin code {label}."
    )
    desc = get_desc(label)

    # And just throw away the image
    tf.io.gfile.remove(photo_filename)

    response = {
        "sentence": sentence,
        "desc": desc
    }
    return response, 200


# Provides access to objects in shell without needing to import manually
@app.shell_context_processor
def make_shell_context():
    return {
        'db': db,
        'User': User,
        'Trophies': Trophies,
    }


if __name__ == '__main__':
    # Create database tables
    db.create_all()
    app.run(host='0.0.0.0', port=80)
