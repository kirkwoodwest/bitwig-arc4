package com.kirkwoodwest.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bitwig.extension.api.opensoundcontrol.OscAddressSpace;
import com.bitwig.extension.api.opensoundcontrol.OscConnection;
import com.bitwig.extension.api.opensoundcontrol.OscModule;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Preferences;
import com.bitwig.extension.controller.api.SettableStringValue;
import com.bitwig.extension.controller.api.Signal;

import static java.lang.Float.isNaN;
import static java.lang.Integer.parseInt;

public class SerialOsc {

  private final OscModule oscModule;
  private final Preferences preferences;
  private final ControllerHost host;
  OscConnection serialOscConnection;
  OscConnection deviceConnection;
  OscConnection listenerConnection;
  SettableStringValue portSetting = null;

  private int serialOscPort = 12002;
  private int listenerPort = 19996;
  private int devicePort = 0;

  String hostname = "127.0.0.1";
  String prefix = "/bitwig";

  // Variables to hold arc/grid LED state
  int[] encs = new int[4];
  int[][] keys = new int[16][16];
  private ArrayList<ArcOscListener> arcListeners = new ArrayList<>();

  ArrayList<String> oscTargets = new ArrayList<>();
  ArrayList<List> oscMessages = new ArrayList<>();

  public SerialOsc(ControllerHost host) {
    this.host = host;
    oscModule = host.getOscModule();
    preferences = host.getPreferences();

    portSetting = preferences.getStringSetting("Device Port", "Connection", 5, "15000");
    portSetting.addValueObserver((newValue)-> {
      // Create a preference for the UDP port for the device we want to connect to.
      int v = parseInt(newValue);
      if (isNaN(v) || (v != 0 && v < 1024) || v > 65535) {
        // Ignore string that are not valid OSC ports, or 0 for "no device"
        portSetting.set(String.valueOf(devicePort));
      }
      else if (parseInt(newValue) != devicePort) {
        // Bitwig requires that all OSC connections are set up during init(),
        // so we need to restart the extension whenever the port changes.
        host.restart();
      }
    });

    // Create a push button that will request a list of connected devices from serialosc and autofill the correct port.
    Signal detect = preferences.getSignalSetting("Detect", "Connection", "Detect Device Port");
    detect.addSignalObserver(()->{
      portSetting.set("0");
      if (serialOscConnection != null) {
        sendMessage(serialOscConnection, "/serialosc/list", hostname, listenerPort);
      }
    });

    // Create our OSC listener to respond to serialosc global and device messages
    OscAddressSpace listenerAddressSpace = oscModule.createAddressSpace();
    listenerAddressSpace.setShouldLogMessages(true);

    // This will support one arc or one grid. If you want to support multiple devices,
    // you'll need fancier prefix and connection management.
    registerGlobalMethods(listenerAddressSpace);
    registerArcMethods(prefix, listenerAddressSpace);
    registerGridMethods(prefix, listenerAddressSpace);

    // Create our UDP/OSC connections: outgoing to serialosc and device, incoming to listener.
    try {
      serialOscConnection = oscModule.connectToUdpServer(hostname, serialOscPort, null);
    } catch(Exception e) {
      host.println("Got exception connecting to serialosc port " + serialOscPort + ": " + e.getMessage());
    }

    try {
      oscModule.createUdpServer(listenerPort, listenerAddressSpace);
    } catch(Exception e) {
      host.println("Got exception creating listener port " + listenerPort + ": " + e.getMessage());
    }

    try {
      devicePort = parseInt(portSetting.get());
      if (devicePort != 0) {
        deviceConnection = connectToDevice(devicePort);
      }
    } catch(Exception e) {
      host.println("Got exception connecting to device port " + devicePort + ": " + e.getMessage());
    }

  }

  private int clamp(float a, int min, int max) {
    return (int) Math.min(Math.max(a, min), max);
  };

  public OscConnection getDeviceConnection() {
    return deviceConnection;
  }

  public void sendMessage(OscConnection oscConnection, String target, Object... message) {
    try {
      oscConnection.sendMessage(target, message);
    } catch (IOException e) {
      host.println("IO Exception:" + e);
    }
  }

  public void addMessageToQueue(String target, Object... message) {
    oscTargets.add(prefix + target);
    if (message.length > 1) {
      List<Object> new_message = new ArrayList<>();
      for (Object o : message) {
        new_message.add(o);
      }
      oscMessages.add(new_message);
    } else {
      List<Object> new_message = new ArrayList<>();
      new_message.add(message[0]);
      oscMessages.add(new_message);
    }
  }

  public void sendQueue() {
    int size = oscTargets.size();

    for(int i = 0; i < size; i++) {
      String target = oscTargets.get(i);
      Object message  = oscMessages.get(i);
      host.println("sendQueue: OSC OUT" + target + " : " + message.toString());
      if( deviceConnection != null) {
        try {
          deviceConnection.sendMessage(target, ((List<?>) message).toArray());

        } catch (IOException e) {
          host.println("IO Exception:" + e);
          host.println("IO Exception:" + target);
        }
      }
    }

    host.println("Queued messages sent");
    oscTargets.clear();
    oscTargets.clear();
  }

  private void onConnection(OscConnection device) {
    if (device != null) {
      // for grids, clear the LEDs and enable the tilt sensor (if present)
      sendMessage(device, prefix + "/grid/led/level/all", 0);
      sendMessage(device, prefix + "/tilt/set", 0, 1);

      // for arcs, clear all LEDs except the first
      for (int i = 0; i < 4; i++) {
      sendMessage(device, prefix + "/ring/all", i, 0);
      sendMessage(device, prefix + "/ring/set", i, 0, 15);
      }
    }
  }

  private void onEncoderKey(int encoder, int state) {
  }

  private void onGridKey(int x, int y, int s) {
    // Example of responding to an event with LED feedback: toggle LED on keypress.
    if (deviceConnection != null) {
      if (s == 1) {
        keys[x][y] = keys[x][y] ^ 1; //flip the bit
        sendMessage(deviceConnection, prefix + "/grid/led/level/set", x, y, 15);
      } else {
        int brightness = (keys[x][y] == 1) ? 6 : 0;
        sendMessage(deviceConnection,prefix + "/grid/led/level/set", x, y, brightness);
      }
    }
  }

  private void onTilt(int sensor, int x, int y, int z) {
  }

  void onDeviceInfo(String id, String type, int port) {
    if (port != devicePort) {
      if (portSetting != null) {
        portSetting.set(String.valueOf(port));
      }

      // Bitwig requires that all OSC connections are set up during init(),
      // so we need to restart the extension whenever the port changes.
      host.restart();
    }
  }

  private OscConnection connectToDevice(int port) {
    if (isNaN(port) || port < 1024 || port > 65535) {
      host.println("Invalid port " + port);
      return null;
    }

    host.println("Connecting to device on port " + port);

    OscConnection device = host.getOscModule().connectToUdpServer(hostname, port, host.getOscModule().createAddressSpace());
    if (device != null) {
      try {
        device.sendMessage("/sys/host", hostname);
        device.sendMessage("/sys/port", listenerPort);
        device.sendMessage("/sys/prefix", prefix);
      } catch (Exception e) {
        host.println("Could not send: /sys/host, /sys/port, /sys/prefix" + devicePort + ": " + e.getMessage());
      }
    }

    onConnection(device);

    return device;
  }

  private void registerGlobalMethods(OscAddressSpace addressSpace) {

    addressSpace.registerMethod("/serialosc/device", ",ssi", "device info", (source, message)-> {
      String id = message.getString(0);
      String type = message.getString(1);
      int port = message.getInt(2);
      host.println("device detected: " + id + " type: " + type + " port: " + port);
      onDeviceInfo(id, type, port);
    });
  }

  private void registerArcMethods(String prefix, OscAddressSpace addressSpace) {

    addressSpace.registerMethod(prefix + "/enc/delta", ",ii", "encoder position change", (source, message) -> {
      int encoder = message.getInt(0);
      int delta = message.getInt(1);
      arcListeners.forEach(arcOscListener -> {
        arcOscListener.onEncoderDelta(encoder, delta);
      });
    });

    addressSpace.registerMethod(prefix + "/enc/key", ",ii", "encoder key state change", (source, message) -> {

      int encoder = message.getInt(0);
      int state = message.getInt(1);
      arcListeners.forEach(arcOscListener -> {
        arcOscListener.onEncoderKey(encoder, state);
      });
      onEncoderKey(encoder, state);
    });
  }

  private void registerGridMethods(String prefix, OscAddressSpace addressSpace) {

    addressSpace.registerMethod(prefix + "/grid/key", ",iii", "grid key state change", (source, message) -> {
      int x = message.getInt(0);
      int y = message.getInt(1);
      int s = message.getInt(2);
      ;
      host.println("key (" + x + "," + y + ") " + (s == 1 ? "down" : "up"));
      onGridKey(x, y, s);
    });

    addressSpace.registerMethod(prefix + "/tilt", ",iiii", "tilt sensor position change", (source, message) -> {
      int sensor = message.getInt(0);
      int x      = message.getInt(1);
      int y      = message.getInt(2);
      int z      = message.getInt(3);
      host.println("tilt " + sensor + " (" + x + "," + y + "," + z + ")");

      onTilt(sensor, x, y, z);
    });
  }

  public void addArcListener(ArcOscListener arcOscListener) {
    arcListeners.add(arcOscListener);
  }
}
