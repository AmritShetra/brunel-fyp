import cv2
import numpy as np
import os
import tensorflow as tf
import matplotlib.pyplot as plt

from sklearn.model_selection import train_test_split
from tensorflow.python.keras.layers import Activation, Flatten, Dense, \
    Conv2D, MaxPooling2D


def visualise_dataset():
    for i in range(9):
        plt.subplot(3, 3, i + 1)
        plt.imshow(X_train[i], cmap='gray', interpolation='none')
        plt.title("Class {}".format(y_train[i]))
        plt.show()


height = 100
width = 100

# Initialise empty arrays
X = []
y = []

# The labels we'll be classifying into
categories = [num for num in range(1, 8)]

# Load up images and add to the training data as necessary
for cat in categories:
    path = 'images/%s/' % cat
    for img in os.listdir(path):
        img_pixels = cv2.imread(path + img, cv2.IMREAD_GRAYSCALE)
        img_pixels = cv2.resize(img_pixels, (height, width))
        X.append(img_pixels)
        y.append(cat)
        print('> Loaded %s (%s) -> %s' % (img, cat, img_pixels.shape))

# Convert X and y to numpy arrays
X = np.array(X)
y = np.array(y)

# Reshape X (length, height, width, 1)
X = np.array(X).reshape(-1, height, width, 1)

# Create train/test datasets from X and y
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25,
                                                    random_state=0)

# Normalise data
X_train = X_train / 255.0
X_test = X_test / 255.0

# visualise_dataset()

# Build the model
model = tf.keras.Sequential()

# Layers
model.add(Conv2D(16, (3, 3), input_shape=X_train.shape[1:]))
model.add(Activation("relu"))
model.add(MaxPooling2D(pool_size=(2, 2)))

model.add(Flatten(input_shape=(height, width)))
model.add(Dense(128, activation='relu'))

# How many classes we have
model.add(Dense(8))
model.add(Activation("softmax"))

# Compile the model with loss function, optimizer & metrics
model.compile(optimizer="adam",
              loss=tf.keras.losses.SparseCategoricalCrossentropy(
                  from_logits=True),
              metrics=['accuracy'])

# Train the model
model.fit(X_train, y_train, epochs=10)

# Evaluate accuracy
test_loss, test_acc = model.evaluate(X_test, y_test, verbose=2)
print('Test Accuracy: {0:.2f}'.format(test_acc * 100))

# Make predictions
img = cv2.imread('Amrit_app_image.png', cv2.IMREAD_GRAYSCALE)
img = cv2.resize(img, (height, width))
img = img.reshape(-1, height, width, 1)
prediction = model.predict(img)  # Predicts each label
print(np.argmax(prediction[0]))  # Get label with highest confidence value
