#!/bin/bash

# Check if the model already exists
FILE=my_model/
if [ ! -d "$FILE" ]; then
  python classifier.py
fi

exec "$@"