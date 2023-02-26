package com.kirkwoodwest.interfaces;

import com.bitwig.extension.controller.api.*;

import java.util.ArrayList;

public class Arc implements ArcOscListener {
  private final CursorTrack cursorTrack;
  private final PinnableCursorDevice cursorDevice;
  private final ArrayList<Parameter> parameters = new ArrayList<>();
  private final SettableRangedValue settingSpeed;
  private ControllerHost host;
  private SerialOsc serialOsc;
  private int encoderSpeed;


  /*
  TO: arc
  -------------------------------------------------------------------------------------------------------
  led 0 is north. clockwise increases led number. These can be viewed and tested in the browser at http://nomeist.com/osc/arc/

    /ring/set n x l

    set led x (0-63) on encoder n (0-1 or 0-3) to level l (0-15)

    /ring/all n l

    set all leds on encoder n (0-1 or 0-3) to level l (0-15)

    /ring/map n l[64]

    set all leds on encoder n (0-1 or 0-3) to 64 member array l[64]

    /ring/range n x1 x2 l

    set leds on encoder n (0-1 or 0-3) between (inclusive) x1 and x2 to level l (0-15). direction of set is always clockwise, with wrapping.

  FROM: arc
  -------------------------------------------------------------------------------------------------------
  arc

  /enc/delta n d
  position change on encoder n by value d (signed). clockwise is positive.

  /enc/key n s
  key state change on encoder n to s (0 or 1, 1 = key down, 0 = key up)
   */


  public Arc(ControllerHost host, SerialOsc serialOsc) {
    this.host = host;
    this.serialOsc = serialOsc;

    serialOsc.addArcListener(this);

    cursorTrack = host.createCursorTrack("Arc", "Arc", 0, 0, true);
    cursorDevice = cursorTrack.createCursorDevice("Arc Device", "Arc Device", 0, CursorDeviceFollowMode.FOLLOW_SELECTION);
    CursorRemoteControlsPage remotePage = cursorDevice.createCursorRemoteControlsPage("Remote Page", 4, "");

    for (int i = 0; i < 4; i++) {
      RemoteControl parameter  = remotePage.getParameter(i);
      parameter.setIndication(true);
      final int ringIndex = i;
      parameter.value().addValueObserver((v)->this.observe(ringIndex, v));
      parameters.add(parameter);
    }

    // /enc/delta n d
    //new LedOscParameter(()->{return parameter}, oscHost, "")
    //speed control

    settingSpeed = this.host.getDocumentState().getNumberSetting("Encoder Speed", "Encoder Speeds", 32, 2048, 32, "", 128);
    changeSpeed(settingSpeed.get());
    settingSpeed.addValueObserver(this::changeSpeed);
  }

  private void changeSpeed(double v) {
    this.encoderSpeed = (int) ((v * (2048 - 32)) + 32);
  }

  private void observe(int ringIndex, double v) {
    int[] data = translateToLedValuesBitwigStyle(v);
    data = circularShift(data, 41);

    for(int i = 0; i < data.length;i++) {
      serialOsc.sendMessage(serialOsc.getDeviceConnection(), "/bitwig/ring/set", ringIndex, i, data[i]);
    }
  }

  private int[] translateToLedValuesBitwigStyle(double v) {
    int[] arr = new int[64];
    int targetLedFill = 47;
    arr[63] = 15;
    arr[47] = 15;
    // calculate the number of elements to fill with full range
    int numFullRangeElements = (int) (v * targetLedFill);

    // calculate the fractional part of the float value
    double fractionalPart = v * targetLedFill - numFullRangeElements;

    // fill the array with full range values
    for (int i = 0; i < numFullRangeElements; i++) {
      arr[i] = 15;
    }

    // fill the next element with the fractional part
    if (numFullRangeElements < targetLedFill) {
      arr[numFullRangeElements] = (int) (fractionalPart * 15);
    }
    return arr;
  }

  private int[] translateToLedValues(double v) {
    int[] arr = new int[64];

    // calculate the number of elements to fill with full range
    int numFullRangeElements = (int) (v * 64);

    // calculate the fractional part of the float value
    double fractionalPart = v * 64 - numFullRangeElements;

    // fill the array with full range values
    for (int i = 0; i < numFullRangeElements; i++) {
      arr[i] = 15;
    }

    // fill the next element with the fractional part
    if (numFullRangeElements < 64) {
      arr[numFullRangeElements] = (int) (fractionalPart * 15);
    }
    return arr;
  }

  private int[] circularShift(int[] arr, int shift) {
    int[] shiftedArr = new int[arr.length];
    for (int i = 0; i < arr.length; i++) {
      shiftedArr[(i + shift) % arr.length] = arr[i];
    }
    return shiftedArr;
  }

  @Override
  public void onEncoderDelta(int encoder, int delta) {
    Parameter parameter = parameters.get(encoder);
    double change = (double) delta / encoderSpeed;
    double targetValue = parameter.value().get() + (change);
    targetValue = Math.max(0, Math.min(1, targetValue));
    if(targetValue < 0) {
      targetValue = 0;
    }
    parameter.value().set(targetValue);
  }

  @Override
  public void onEncoderKey(int encoder, int state) {
    //not implemented
  }
}


