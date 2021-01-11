from flask_sqlalchemy import SQLAlchemy
import flask

app = flask.Flask(__name__)
app.config["DEBUG"] = False
app.config["SQLALCHEMY_DATABASE_URI"] = 'sqlite:///database.db'
db = SQLAlchemy(app)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100))

    def __repr__(self):
        return f"User {self.id}: {self.username}"

# Tables must be created after models are defined.
db.create_all()

@app.route('/')
def home():
    return "Hello World!"


# https://stackoverflow.com/questions/7023052/configure-flask-dev-server-to-be-visible-across-the-network
if __name__ == '__main__':
    app.run(host='0.0.0.0')

