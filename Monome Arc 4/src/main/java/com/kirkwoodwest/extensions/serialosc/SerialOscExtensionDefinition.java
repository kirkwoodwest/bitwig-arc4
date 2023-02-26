package com.kirkwoodwest.extensions.serialosc;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

public class SerialOscExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("313bad03-1d10-4204-862e-0751f3f9bf8e");
   
   public SerialOscExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "Serial Osc";
   }
   
   @Override
   public String getAuthor()
   {
      return "Basic Programmer & Dewb";
   }

   @Override
   public String getVersion()
   {
      return "0.2";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }
   
   @Override
   public String getHardwareVendor()
   {
      return "Monome";
   }
   
   @Override
   public String getHardwareModel()
   {
      return "Serial Osc";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 17;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 0;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 0;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
   }

   @Override
   public serialOscExtension createInstance(final ControllerHost host)
   {
      return new serialOscExtension(this, host);
   }
}
