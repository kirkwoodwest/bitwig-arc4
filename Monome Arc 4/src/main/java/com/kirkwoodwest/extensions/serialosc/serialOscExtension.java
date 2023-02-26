package com.kirkwoodwest.extensions.serialosc;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import com.kirkwoodwest.interfaces.SerialOsc;

public class serialOscExtension extends ControllerExtension
{

   private ControllerHost host;

   protected serialOscExtension(final SerialOscExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      host = getHost();

      // TODO: Perform your driver initialization here.
      SerialOsc serialOsc = new SerialOsc(host);
      host.showPopupNotification("Serial Osc Initialized");
   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      getHost().showPopupNotification("Monome Arc 4 Exited");
   }

   @Override
   public void flush()
   {
      //oscHost.addMessageToQueue("/ring/led", 2, 2, 1);
//      int[] myInt = {0, 2, 4, 2};
//      oscHost.addMessageToQueue("/ring/int", myInt);
//      oscHost.addMessageToQueue("/ring/int", 2);
      // TODO Send any updates you need here.
   }


}
