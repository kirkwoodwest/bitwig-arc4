package com.kirkwoodwest.extensions.monomeArc4;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class MonomeArc4ExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("313bad01-5d10-4204-862d-8754f3f1bf8e");
   
   public MonomeArc4ExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "Monome Arc 4";
   }
   
   @Override
   public String getAuthor()
   {
      return "Kirkwood West";
   }

   @Override
   public String getVersion()
   {
      return "0.1.1";
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
      return "Monome Arc 4";
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
   public MonomeArc4Extension createInstance(final ControllerHost host)
   {
      return new MonomeArc4Extension(this, host);
   }
}
