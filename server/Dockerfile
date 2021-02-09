FROM python:3.8.0

ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

WORKDIR /code/
COPY requirements.txt \
     app.py models.py config.py my_model/ /code/

RUN pip install --upgrade pip
RUN pip install -r requirements.txt

CMD python app.py

