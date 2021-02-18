import base64
import flask_testing
import unittest

from app import app, db
from models import User, Trophies


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

        user_id = User.query.filter_by(username=user.username).one().id
        trophies = Trophies(
            user_id=user_id
        )
        db.session.add(trophies)

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

    def test_update_user(self):
        # Make another account so we can test username/email clashes
        new_user = User(
            username='new_username',
            password='new_password',
            email='new_email',
            first_name='new_first_name',
            last_name='new_last_name'
        )
        db.session.add(new_user)

        # The form sent by the app, only field being updated is username
        data = {
            "username": "new_username",
            "password": "valid_password",
            "email": "valid_email",
            "first_name": "valid_first_name",
            "last_name": "valid_last_name"
        }
        credentials = base64.b64encode(b"valid_username:valid_password") \
            .decode('utf-8')
        response = self.client.put(
            "/users/edit/",
            json=data,
            headers={"Authorization": f"Basic {credentials}"}
        )

        # Username belongs to the newly created user, so it raises a 409 error
        self.assertStatus(response, 409)

        # Same logic as email, so move on
        # Test if it works with valid input
        data['username'] = "a_new_username"
        response = self.client.put(
            "/users/edit/",
            json=data,
            headers={"Authorization": f"Basic {credentials}"}
        )

        self.assert200(response)

    def test_get_trophies(self):
        credentials = base64.b64encode(b"valid_username:valid_password") \
            .decode('utf-8')
        response = self.client.get(
            "/trophies/",
            headers={"Authorization": f"Basic {credentials}"}
        )

        # Should return JSON with trophies being locked/false
        self.assert200(response)
        self.assertEqual(response.json, {
            "trophy_one": False,
            "trophy_two": False
        })

    def test_update_trophies(self):
        data = {
            "trophy_name": "trophy_one"
        }

        credentials = base64.b64encode(b"valid_username:valid_password") \
            .decode('utf-8')
        response = self.client.put(
            "/trophies/",
            json=data,
            headers={"Authorization": f"Basic {credentials}"}
        )

        self.assert200(response)
        self.assertTrue(Trophies.query.filter_by(user_id=1).one().trophy_one)

        # Try it again
        response = self.client.put(
            "/trophies/",
            json=data,
            headers={"Authorization": f"Basic {credentials}"}
        )
        # Should raise 304 error as trophy has already been unlocked
        self.assertStatus(response, 304)


if __name__ == '__main__':
    unittest.main()
