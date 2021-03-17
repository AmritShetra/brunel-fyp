#!/bin/bash

# Check if the model already exists
FILE=my_model/
if [ ! -d "$FILE" ]; then
  python classifier.py
else
  echo "Image classifier already exists... moving on."
fi

exec "$@"