package com.kirkwoodwest.interfaces;

public interface ArcOscListener {
  void onEncoderDelta(int encoder, int delta);
  void onEncoderKey(int encoder, int state);
}
