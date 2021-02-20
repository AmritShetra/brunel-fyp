import numpy as np
import tensorflow as tf
import werkzeug
from flask import Flask, jsonify, request
from config import Config
from models import db, User, Trophies

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

    # Save to the database
    db.session.commit()
    response['result'] = "{} added to database.".format(user.username)

    return response, 200


def check_credentials(username, password):
    response = {}
    # Checking if the username exists beforehand
    user = User.query.filter_by(username=username).first()
    if not user:
        response['result'] = "Username not found."
        return response, 401

    if user.password == password:
        response['result'] = "Authenticated."
        return response, 200
    else:
        response['result'] = "Please check your login details and try again."
        return response, 401


@app.route('/login/', methods=['POST'])
def login():
    data = request.json

    return check_credentials(
        data['username'],
        data['password']
    )


@app.route('/users/', methods=['GET'])
def get_user():
    username = request.authorization.username
    password = request.authorization.password

    response, status = check_credentials(username, password)

    if status == 401:
        return response, status

    user = User.query.filter_by(username=username).first()
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

    response, status = check_credentials(username, password)

    if status == 401:
        return response, status

    user = User.query.filter_by(username=username).first()
    # If username is being changed, check if the new one already exists
    if not user.username == data['username']:
        if User.query.filter_by(username=data['username']).first():
            response['error'] = "Username is taken."
            return response, 409

    # Same thing with email
    if not user.email == data['email']:
        if User.query.filter_by(email=data['email']).first():
            response['error'] = "Email is taken."
            return response, 409

    # Looks like everything is fine, update the fields
    user.username = data['username']
    user.password = data['password']
    user.email = data['email']
    user.first_name = data['first_name']
    user.last_name = data['last_name']

    db.session.commit()
    response['result'] = "User model updated."
    return response, 200


@app.route('/trophies/', methods=['GET'])
def get_trophies():
    username = request.authorization.username
    password = request.authorization.password

    response, status = check_credentials(username, password)

    if status == 401:
        return response, status

    user = User.query.filter_by(username=username).first()
    # Send the account's trophies over
    trophies = Trophies.query.filter_by(user_id=user.id).first()

    data = {
        "trophy_one": trophies.trophy_one,
        "trophy_two": trophies.trophy_two
    }

    return data, 200


@app.route('/trophies/', methods=['PUT'])
def update_trophies():
    username = request.authorization.username
    password = request.authorization.password

    response, status = check_credentials(username, password)

    if status == 401:
        return response, status

    user = User.query.filter_by(username=username).first()

    # Check if the given trophy is already "unlocked"
    data = request.json
    key = data['trophy_name']
    trophies = Trophies.query.filter_by(user_id=user.id).first()
    if getattr(trophies, key):  # https://stackoverflow.com/a/54985920
        response['error'] = "Trophy is already unlocked."
        return response, 304

    # "Unlock" the trophy if necessary
    # https://stackoverflow.com/questions/23152337/how-to-update-sqlalchemy-orm-object-by-a-python-dict
    setattr(trophies, key, True)
    db.session.commit()

    response['result'] = "You've unlocked an achievement - well done!"
    return response, 200


@app.route('/classify/', methods=['POST'])
def process_photo():
    photo = request.files["photo"]
    photo_filename = werkzeug.utils.secure_filename(photo.filename)
    photo.save(photo_filename)

    from classifier import img_height, img_width
    from labels import labels, get_desc

    model = tf.keras.models.load_model('my_model')

    # Load a new image
    img = tf.keras.preprocessing.image.load_img(
        photo_filename,
        color_mode='grayscale',
        target_size=(img_height, img_width)
    )
    # Turn it into an array
    img_array = tf.keras.preprocessing.image.img_to_array(img)
    # Create a batch/list to give to the model
    img_array = tf.expand_dims(img_array, 0)

    # Prediction
    pred = model.predict(img_array)
    # Get the value with the highest confidence
    score = tf.nn.softmax(pred[0])

    # The value is used as an index in our list of classes/labels
    label = labels[np.argmax(score)]
    confidence = np.max(score)

    sentence = "I've taken a quick look. \n" + \
               "This image is {}% likely to be resin code {}.".format(
                   int(confidence * 100), label
               )
    desc = get_desc(label)

    # And just throw away the image
    tf.io.gfile.remove(
        photo_filename
    )

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


# https://stackoverflow.com/questions/7023052/configure-flask-dev-server-to-be-visible-across-the-network
if __name__ == '__main__':
    # Create database tables
    db.create_all()
    app.run(host='0.0.0.0')
