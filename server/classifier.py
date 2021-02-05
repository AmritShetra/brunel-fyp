import cv2
import numpy as np
import os
import tensorflow as tf

from sklearn.model_selection import train_test_split
from tensorflow.python.keras.layers import Activation, Flatten, Dense

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

# Normalise data
X = X.astype("float") / 255.0
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25,
                                                    random_state=100)

# Build the model
model = tf.keras.Sequential()

# Layer
model.add(Flatten(input_shape=(height, width)))
model.add(Dense(8, activation='relu'))
model.add(Activation("softmax"))

# Compile the model with loss function, optimizer & metrics
model.compile(loss="sparse_categorical_crossentropy", optimizer="adam",
              metrics=["accuracy"])

# Train the model
model.fit(X_train, y_train, epochs=10)

# Evaluate accuracy
test_loss, test_acc = model.evaluate(X_test, y_test, verbose=2)
print('\n Test Accuracy:', test_acc * 100)
