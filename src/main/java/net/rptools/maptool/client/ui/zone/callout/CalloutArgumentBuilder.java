package net.rptools.maptool.client.ui.zone.callout;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalloutArgumentBuilder {
  private Color textColor;
  private Color backgroundColor;
  private Color outlineColor;
  private CalloutPopupLocation popupLocation;
  private final List<String> otherText = new ArrayList<>();
  private final Map<String, String> otherArguments = new HashMap<>();

  public CalloutArguments build() {
    return new CalloutArguments(
        textColor, backgroundColor, outlineColor, popupLocation, otherText, otherArguments);
  }

  public Color getTextColor() {
    return textColor;
  }

  public CalloutArgumentBuilder setTextColor(Color textColor) {
    this.textColor = textColor;
    return this;
  }

  public CalloutArgumentBuilder setTextColor(String textColor) {
    this.textColor = Color.decode(textColor);
    return this;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public CalloutArgumentBuilder setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
    return this;
  }

  public CalloutArgumentBuilder setBackgroundColor(String backgroundColor) {
    this.backgroundColor = Color.decode(backgroundColor);
    return this;
  }

  public Color getOutlineColor() {
    return outlineColor;
  }

  public CalloutArgumentBuilder setOutlineColor(Color outlineColor) {
    this.outlineColor = outlineColor;
    return this;
  }

  public CalloutArgumentBuilder setOutlineColor(String outlineColor) {
    this.outlineColor = Color.decode(outlineColor);
    return this;
  }

  public CalloutPopupLocation getPopupLocation() {
    return popupLocation;
  }

  public CalloutArgumentBuilder setPopupLocation(CalloutPopupLocation popupLocation) {
    this.popupLocation = popupLocation;
    return this;
  }

  public List<String> getOtherText() {
    return otherText;
  }

  public CalloutArgumentBuilder addText(String otherText) {
    this.otherText.add(otherText);
    return this;
  }

  public CalloutArgumentBuilder clearText() {
    this.otherText.clear();
    return this;
  }

  public Map<String, String> getOtherArguments() {
    return otherArguments;
  }

  public CalloutArgumentBuilder putOtherArguments(String name, String value) {
    otherArguments.put(name, value);
    return this;
  }

  public CalloutArgumentBuilder removeOtherArgument(String name) {
    otherArguments.remove(name);
    return this;
  }
}
