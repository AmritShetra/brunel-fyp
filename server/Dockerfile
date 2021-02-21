FROM python:3.8.0

ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

WORKDIR /code/
COPY requirements.txt \
     app.py \
     models.py \
     config.py \
     classifier.py \
     tests.py /code/

COPY images /code/images/

RUN pip install --upgrade pip
RUN pip install -r requirements.txt
