import base64
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
            username="valid_username",
            password="valid_password",
            email="valid_email@email.com",
            first_name="valid_first_name",
            last_name="valid_last_name"
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

        user = User.query.filter_by(username="valid_username").first()
        self.assertEqual(user.username, "valid_username")

    def test_create_user(self):
        # Pass data identical to the test user's account
        data = {
            "username": "valid_username",
            "password": "valid_password",
            "email": "valid_email@email.com",
            "first_name": "valid_first_name",
            "last_name": "valid_last_name"
        }
        response = self.client.post("/register/", json=data)

        # Username/email is taken
        self.assert_status(response, 409)

        # Change the username (but the email is still taken)
        data['username'] = 'Amrit'
        response = self.client.post("/register/", json=data)
        self.assert_status(response, 409)

        # Change the email too
        data['email'] = 'adifferentemail@email.com'
        response = self.client.post("/register/", json=data)
        self.assert_status(response, 200)

        # Make sure there's a new User in the database
        new_user = User.query.filter_by(username='Amrit').one()
        self.assertEqual(new_user.username, 'Amrit')

    def test_login(self):
        data = {
            "username": "invalid_username",
            "password": "valid_password",
        }
        response = self.client.post("/login/", json=data)

        # An invalid username (i.e. 'not found') should return 401
        self.assert401(response)

        # Swap to a valid username, but this time, use the wrong password
        data['username'] = "valid_username"
        data['password'] = "invalid_password"
        response = self.client.post("/login/", json=data)
        self.assert401(response)

        # And finally, use the correct password
        data['password'] = "valid_password"
        response = self.client.post("/login/", json=data)
        self.assert200(response)

    def test_get_user(self):
        # If credentials match, user data should be returned
        # No need to check invalid credentials, login route covers it

        # https://stackoverflow.com/a/30250045
        credentials = base64.b64encode(b"valid_username:valid_password")\
            .decode('utf-8')
        response = self.client.get(
            "/users/", headers={"Authorization": f"Basic {credentials}"}
        )

        self.assert200(response)


if __name__ == '__main__':
    unittest.main()
