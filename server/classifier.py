import tensorflow as tf
import matplotlib.pyplot as plt

# Parameters for loading images
batch_size = 32
img_height = 100
img_width = 100

if __name__ == '__main__':
    # Training dataset - 75%
    train_ds = tf.keras.preprocessing.image_dataset_from_directory(
        'images/',
        validation_split=0.25,
        subset="training",
        seed=123,
        image_size=(img_height, img_width),
        batch_size=batch_size
    )

    # Validation dataset - 25% split
    val_ds = tf.keras.preprocessing.image_dataset_from_directory(
        "images/",
        validation_split=0.25,
        subset="validation",
        seed=123,
        image_size=(img_height, img_width),
        batch_size=batch_size
    )

    # 1 - 7
    class_names = train_ds.class_names

    # Show a few images and their labels
    plt.figure(figsize=(10, 10))
    for images, labels in train_ds.take(1):
        for i in range(9):
            ax = plt.subplot(3, 3, i + 1)
            plt.imshow(images[i].numpy().astype("uint8"))
            plt.title(class_names[labels[i]])
            plt.axis("off")
            # plt.show()

    # Reduce values from 0-255 to 0-1 range by scaling down the images
    normalisation_layer = tf.keras.layers.experimental.preprocessing.Rescaling(
        1./255
    )

    num_classes = len(class_names)

    # Create the CNN
    model = tf.keras.Sequential([
        normalisation_layer,
        tf.keras.layers.Conv2D(32, 3, activation='relu'),
        tf.keras.layers.MaxPooling2D(),

        tf.keras.layers.Conv2D(32, 3, activation='relu'),
        tf.keras.layers.MaxPooling2D(),

        tf.keras.layers.Conv2D(32, 3, activation='relu'),
        tf.keras.layers.MaxPooling2D(),

        tf.keras.layers.Flatten(),
        tf.keras.layers.Dense(128, activation='relu'),

        #
        tf.keras.layers.Dense(num_classes)
    ])

    model.compile(
        optimizer='adam',
        loss=tf.losses.SparseCategoricalCrossentropy(from_logits=True),
        metrics=['accuracy']
    )

    # Train the model
    model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=10
    )

    # Get accuracy
    test_loss, test_acc = model.evaluate(val_ds)
    print('Test Accuracy: {0:.2f}'.format(test_acc * 100))

    # Save model to disk
    model.save('my_model')
