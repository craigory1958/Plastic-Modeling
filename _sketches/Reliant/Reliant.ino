

#include <Arduino.h>

#include <AnalogInputDevice.h>
#include <ChannelValue.h>
#include <DeviceManager.h>
#include <DigitalOutputDevice.h>


const int strobePin = 9;
int strobeTransitions[] = { 50, 100, 50, 750, 0 };

const int navPin = 8;
int navTransitions[] = { 300, 1000, 0 };

const int interiorPin = 10;
int interiorTransitions[] = { -1 };

const int intensityPin = PIN_A7;
ChannelValue intensity = ChannelValue(25);

DeviceManager manager = DeviceManager();


void setup() {

  manager.add(AnalogInputDevice(intensityPin, &intensity));

  manager.add(DigitalOutputDevice(strobePin, HIGH, strobeTransitions));
  manager.add(DigitalOutputDevice(navPin, HIGH, navTransitions));
  manager.add(DigitalOutputDevice(interiorPin, HIGH, &intensity, interiorTransitions));

   Serial.begin(9600);
}


void loop() {
  manager.execute();
}
