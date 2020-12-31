import flask

app = flask.Flask(__name__)
app.config["DEBUG"] = False


@app.route('/')
def home():
    return "Hello World!"


# https://stackoverflow.com/questions/7023052/configure-flask-dev-server-to-be-visible-across-the-network
if __name__ == '__main__':
    app.run(host='0.0.0.0')
