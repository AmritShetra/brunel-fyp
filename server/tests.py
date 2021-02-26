import base64
import flask_testing
import unittest

from werkzeug.datastructures import FileStorage

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

        # Check that the Trophies object has been created
        trophies = Trophies.query.filter_by(user_id=1).one()
        self.assertEqual(trophies.user_id, 1)

    def test_login(self):
        # An invalid username (i.e. 'not found') should return 401
        data = {
            "username": "invalid_username",
            "password": "valid_password",
        }
        response = self.client.post("/login/", json=data)
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
        data = {
            'username': 'valid_username',
            'password': 'valid_password'
        }
        response = self.client.post("/login/", json=data)
        token = response.json['access_token']

        headers = {
            'Authorization': 'Bearer {}'.format(token)
        }
        response = self.client.get("/users/", headers=headers)
        self.assert200(response)

    def test_update_user(self):
        data = {
            'username': 'valid_username',
            'password': 'valid_password'
        }
        response = self.client.post("/login/", json=data)
        token = response.json['access_token']
        headers = {
            'Authorization': 'Bearer {}'.format(token)
        }

        # Make another account so we can test username/email clashes
        new_user = User(
            username='new_username',
            password='new_password',
            email='new_email',
            first_name='new_first_name',
            last_name='new_last_name'
        )
        db.session.add(new_user)

        # Send a request to update username (which is taken, so returns 409)
        data = {
            "username": "new_username",
            "password": "valid_password",
            "email": "valid_email",
            "first_name": "valid_first_name",
            "last_name": "valid_last_name"
        }
        response = self.client.put("/users/edit/", json=data, headers=headers)
        self.assertStatus(response, 409)

        # Same logic as email, so move on and test if it works with valid input
        data['username'] = "a_new_username"
        response = self.client.put("/users/edit/", json=data, headers=headers)
        self.assert200(response)

    def test_get_trophies(self):
        data = {
            'username': 'valid_username',
            'password': 'valid_password'
        }
        response = self.client.post("/login/", json=data)
        token = response.json['access_token']
        headers = {
            'Authorization': 'Bearer {}'.format(token)
        }

        response = self.client.get("/trophies/", headers=headers)

        # Should return JSON with trophies being locked
        self.assert200(response)
        self.assertEqual(
            response.json,
            {"trophy_one": False, "trophy_two": False}
        )

    def test_update_trophies(self):
        data = {
            'username': 'valid_username',
            'password': 'valid_password'
        }
        response = self.client.post("/login/", json=data)
        token = response.json['access_token']
        headers = {
            'Authorization': 'Bearer {}'.format(token)
        }

        data = {
            "trophy_name": "trophy_one"
        }
        response = self.client.put("/trophies/", json=data, headers=headers)
        self.assert200(response)
        self.assertTrue(Trophies.query.filter_by(user_id=1).one().trophy_one)

        # Try it again - should raise 304 error as trophy is already unlocked
        response = self.client.put("/trophies/", json=data, headers=headers)
        self.assertStatus(response, 304)

    def test_process_photo(self):
        image = "images/1/1_image_one.png"
        image_file = FileStorage(
            stream=open(image, "rb"),
            filename="testing_image.png",
            content_type="image/png"
        )
        data = {
            "photo": image_file
        }

        response = self.client.post(
            "/classify/",
            data=data
        )
        self.assert200(response)

        # Check if the response has the correct prediction
        # response_data = response.get_json()
        # print(response_data)
        # correct_answer = False
        # if "1" in response_data['sentence'].split():
        #     correct_answer = True
        # self.assertTrue(correct_answer)


if __name__ == '__main__':
    unittest.main()
