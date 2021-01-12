import flask
from models import db, User

app = flask.Flask(__name__)
app.config["DEBUG"] = False
app.config["SQLALCHEMY_DATABASE_URI"] = 'sqlite:///database.db'

# Specify which app we are using with SQLAlchemy
app.app_context().push()
db.init_app(app)


@app.route('/')
def home():
    return "Hello World!"


@app.shell_context_processor
def make_shell_context():
    return {'db': db, 'User': User}


# https://stackoverflow.com/questions/7023052/configure-flask-dev-server-to-be-visible-across-the-network
if __name__ == '__main__':
    # Create database tables
    db.create_all()
    app.run(host='0.0.0.0')
