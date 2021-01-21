from flask import Flask, jsonify
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
