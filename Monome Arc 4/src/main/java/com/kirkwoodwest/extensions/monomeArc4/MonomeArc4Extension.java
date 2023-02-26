package com.kirkwoodwest.extensions.monomeArc4;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.ControllerExtension;
import com.kirkwoodwest.interfaces.Arc;
import com.kirkwoodwest.interfaces.SerialOsc;

public class MonomeArc4Extension extends ControllerExtension
{
   private ControllerHost host;
   private SerialOsc serialOsc;
   private Arc arc;

   protected MonomeArc4Extension(final MonomeArc4ExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      host = getHost();

      // TODO: Perform your driver initialization here.
      // For now just show a popup notification for verification that it is running.

      serialOsc = new SerialOsc(host);
      arc = new Arc(host, serialOsc);

      host.showPopupNotification("Monome Arc 4 Initialized");
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
      //serialOsc.sendQueue(); quing doesn't really work right now... don't know why...
   }
}
