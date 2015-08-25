#!/usr/bin/env python
#
# Copyright 2015 Richard Downer
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# This is a simple web service for reading the MCP9808 sensor and controlling the output GPIO pins.
#
# It is hard-coded with these assumptions:
# - The MCP9808 device is on the I2C bus at address 0x18 (this is the default for the MCP9808 if the address lines are
#   left disconnected)
# - GPIO pins 23 and 24 are connected to the relay module
#
# It requires the installation of the Adafruit MCP9808 Pyhton libraries; installation instructions can be found at
# https://learn.adafruit.com/mcp9808-temperature-sensor-python-library/overview
# It also requires the Python "bottle" library to provide the web server; this can be installed with:
#   apt-get install python-bottle
# All other dependencies should be satisfied by the default install state of a recent Raspberry Pi.
#
# Once the dependencies are satisfied, simply run the script (probably as root, to get the required hardware access)
# to start the web server on port 8080.
#
# To manipulate it:
#   curl -X GET http://192.168.2.2:8080/temperature
#   curl -X POST http://192.168.2.2:8080/relay/1/on
#   curl -X POST http://192.168.2.2:8080/relay/1/off
#   curl -X POST http://192.168.2.2:8080/relay/2/on
#   curl -X POST http://192.168.2.2:8080/relay/2/off

from bottle import get, post, request, run, debug
import Adafruit_MCP9808.MCP9808 as MCP9808
import RPi.GPIO as GPIO

sensor = MCP9808.MCP9808()
sensor.begin()

GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)
GPIO.output(23, False)
GPIO.setup(24, GPIO.OUT)
GPIO.output(24, False)

@get('/temperature')
def login():
    return '%f' % sensor.readTempC()

@post('/relay/1/off')
def relay1off():
    GPIO.output(23, False)

@post('/relay/1/on')
def relay1on():
    GPIO.output(23, True)

@post('/relay/2/off')
def relay2off():
    GPIO.output(24, False)

@post('/relay/2/on')
def relay2on():
    GPIO.output(24, True)

debug(True)
run(host='192.168.2.2', port=8080)

