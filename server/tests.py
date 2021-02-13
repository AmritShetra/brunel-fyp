import flask_testing
import unittest

from app import app, db
from models import User


class BaseTest(flask_testing.TestCase):

    def create_app(self):
        test_app = app

        test_app.config['DEBUG'] = False
        test_app.config['TESTING'] = True
        test_app.config['SQLALCHEMY_DATABASE_URI'] = "sqlite://"
        test_app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

        test_app.app_context().push()
        db.init_app(test_app)

        return app

    def setUp(self):
        db.create_all()

        user = User(
            username="AmritShetra",
            password="itsasecret",
            email="my.email@provider.net",
            first_name="Amrit",
            last_name="Shetra"
        )

        db.session.add(user)
        db.session.commit()

    def tearDown(self):
        db.session.remove()
        db.drop_all()


class TestViews(BaseTest):

    def test_home(self):
        response = self.client.get("/")
        self.assert200(response)

        user = User.query.filter_by(username="AmritShetra").first()
        self.assertEqual(user.username, "AmritShetra")

    def test_register(self):
        data = {
            "username": "AmritShetra",
            "password": "itsasecret",
            "email": "my.email@provider.net",
            "first_name": "Amrit",
            "last_name": "Shetra"
        }
        response = self.client.post("/register/", json=data)

        # Username/email is taken
        self.assert_status(response, 409)

        # Change the username (but the email is still taken)
        data['username'] = 'Amrit'
        response = self.client.post("/register/", json=data)
        self.assert_status(response, 409)

        # Change the email too
        data['email'] = 'adifferentemail@provider.net'
        response = self.client.post("/register/", json=data)
        self.assert_status(response, 200)

        # Make sure there's a new User in the database
        new_user = User.query.filter_by(username='Amrit').one()
        self.assertEqual(new_user.username, 'Amrit')


if __name__ == '__main__':
    unittest.main()
