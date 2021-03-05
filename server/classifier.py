import matplotlib.pyplot as plt
import tensorflow as tf


# Parameters for loading images
color_mode = 'grayscale'
img_height = 200
img_width = 200
batch_size = 32


# Show a few images and their labels
def show_images():
    plt.figure(figsize=(10, 10))
    for images, labels in training_dataset.take(1):
        for i in range(9):
            plt.subplot(3, 3, i + 1)
            plt.imshow(images[i], cmap='gray', interpolation='none')
            plt.title(f"Class {class_names[labels[i]]}")
            plt.axis("off")
    plt.show()


if __name__ == '__main__':
    training_dataset = tf.keras.preprocessing.image_dataset_from_directory(
        'images/',
        color_mode=color_mode,
        validation_split=0.25,
        subset="training",
        seed=123,
        image_size=(img_height, img_width),
        batch_size=batch_size
    )

    validation_dataset = tf.keras.preprocessing.image_dataset_from_directory(
        "images/",
        color_mode=color_mode,
        validation_split=0.25,
        subset="validation",
        seed=123,
        image_size=(img_height, img_width),
        batch_size=batch_size
    )

    # 1 - 7
    class_names = training_dataset.class_names

    # show_images()

    # Reduce values from 0-255 to 0-1 range by scaling down the images
    normalisation_layer = tf.keras.layers.experimental.preprocessing.Rescaling(
        1./255
    )

    num_classes = len(class_names)

    # Create the CNN
    model = tf.keras.Sequential([
        normalisation_layer,

        tf.keras.layers.Conv2D(32, 3, padding='same', activation='relu'),
        tf.keras.layers.MaxPooling2D(),

        tf.keras.layers.Conv2D(64, 3, padding='same', activation='relu'),
        tf.keras.layers.MaxPooling2D(),

        tf.keras.layers.Conv2D(128, 3, activation='relu'),
        tf.keras.layers.MaxPooling2D(),

        tf.keras.layers.Conv2D(128, 3, activation='relu'),
        tf.keras.layers.MaxPooling2D(),

        tf.keras.layers.Flatten(),
        tf.keras.layers.Dropout(0.5),
        tf.keras.layers.Dense(256, activation='relu'),

        # Softmax outputs probabilities for each label between 0 and 1
        tf.keras.layers.Dense(num_classes, activation='softmax'),
    ])

    model.compile(
        optimizer='adam',
        loss=tf.losses.SparseCategoricalCrossentropy(from_logits=True),
        metrics=['accuracy']
    )

    # Train the model
    model.fit(
        training_dataset,
        validation_data=validation_dataset,
        epochs=30
    )

    test_loss, test_acc = model.evaluate(validation_dataset)
    print('Test Accuracy: {0:.2f}'.format(test_acc * 100))

    # Save model to disk
    model.save('my_model')
