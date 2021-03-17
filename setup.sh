#!/bin/bash

sudo apt-get -y update

sudo apt -y install docker.io
sudo apt -y install docker-compose

sudo groupadd docker
sudo gpasswd -a $USER docker
newgrp docker
