/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.swing;

import java.beans.*;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;
import net.rptools.lib.MathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for linking a numeric JSpinner, JSlider, and a variable. The slider and spinner are
 * tied together with the class NumericModel. It holds delegates for the two component models which
 * update each other.The default relationship between spinner value and slider value is 1:1 and
 * utilises a Functional Interface to translate values between the two. This can be customised by
 * setting your own Functions, e.g. an example for 1:100
 *
 * <pre>
 * Function&lt;Number, Integer> spinnerToSlider =
 * number -> ((Number) number.doubleValue * 100).intValue<br>
 * Function&lt;Integer, Number> sliderToSpinner =
 * number -> ((Number) i).doubleValue()/100d
 * </pre>
 *
 * <p>Similarly:<br>
 * Providing a <code>Consumer&lt;Number> </code>for a linked property will result in the property
 * being updated with the spinner value.<br>
 * Providing a <code>Supplier&lt;Number></code> for a linked property can be used to update the
 * spinner value by calling <code>update()</code>. PropertyChangeSupport and VetoableChangeSupport
 * have been implemented on the numeric model for the setPairValue method.
 */
public class SpinnerSliderPaired {
  public SpinnerSliderPaired(JSpinner spinner, JSlider slider) {
    this(spinner, slider, null);
  }

  public SpinnerSliderPaired(JSpinner spinner, JSlider slider, Consumer<Number> propertySetter) {
    this(spinner, slider, propertySetter, Number::intValue, (i) -> ((Number) i).doubleValue());
    log.debug("spinner-slider pair using default relationship.");
  }

  public SpinnerSliderPaired(
      JSpinner spinner,
      JSlider slider,
      Consumer<Number> propertySetter,
      Function<Number, Integer> spinnerToSlider,
      Function<Integer, Number> sliderToSpinner) {
    // set functional relationship
    setSpinnerToSlider(spinnerToSlider);
    setSliderToSpinner(sliderToSpinner);
    // set the controls
    setLinkedSpinner(spinner);
    setLinkedSlider(slider);
    // set the property setter
    setPropertySetter(propertySetter);

    commonModel.setDelegateValues();
    getLinkedSlider().setModel(commonModel.sliderModelDelegate);
    getLinkedSpinner().setModel(commonModel.spinnerModelDelegate);
  }

  private static final Logger log = LogManager.getLogger(SpinnerSliderPaired.class);

  protected SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);
  protected VetoableChangeSupport vcs = new VetoableChangeSupport(this);

  public void addVetoableChangeListener(VetoableChangeListener listener) {
    vcs.addVetoableChangeListener(listener);
  }

  protected void removeVetoableChangeListener(VetoableChangeListener listener) {
    vcs.addVetoableChangeListener(listener);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  protected void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  private JSpinner linkedSpinner;
  private JSlider linkedSlider;

  public JSpinner getLinkedSpinner() {
    return linkedSpinner;
  }

  public JSlider getLinkedSlider() {
    return linkedSlider;
  }

  private void setLinkedSlider(@NotNull JSlider slider) {
    this.linkedSlider = slider;
  }

  private void setLinkedSpinner(JSpinner spinner) {
    this.linkedSpinner = spinner;
  }

  // option to set the spinner to loop/wrap at end values
  public void setModelWraps(boolean b) {
    commonModel.modelWraps = b;
  }

  // Functional interface for setting a property
  private Consumer<Number> propertySetter;
  // Functional interface for getting a property
  private Supplier<Number> propertyGetter;
  private String propertyName = "Property";

  public Consumer<Number> getPropertySetter() {
    return propertySetter;
  }

  public Supplier<Number> getPropertyGetter() {
    return propertyGetter;
  }

  public void setPropertySetter(Consumer<Number> propertySetter) {
    this.propertySetter = propertySetter;
  }

  public void setPropertyGetter(Supplier<Number> propertyGetter) {
    this.propertyGetter = propertyGetter;
  }

  public void setPropertyName(String s) {
    this.propertyName = s;
  }

  private void setProperty(Number n) {
    if (propertyGetter != null) {
      if (propertyGetter.get().doubleValue() != n.doubleValue() && propertySetter != null) {
        propertySetter.accept(n);
      }
    }
  }

  public void update() {
    if (propertyGetter != null) {
      setPairValue(propertyGetter.get());
    }
  }

  // Instance of NumericModel that ties the two controls together
  private final NumericModel commonModel = new NumericModel();
  // function that defines the value conversion from slider to spinner, defaults to 1:1
  private Function<Integer, Number> sliderToSpinner = i -> ((Number) i).doubleValue();
  // function that defines the value conversion from spinner to slider
  private Function<Number, Integer> spinnerToSlider = Number::intValue;

  public void setSpinnerToSlider(Function<Number, Integer> function) {
    spinnerToSlider = function;
  }

  public void setSliderToSpinner(Function<Integer, Number> function) {
    sliderToSpinner = function;
  }

  public Number getPairNextValue() {
    return commonModel.getModelNextValue();
  }

  public Number getPairPreviousValue() {
    return commonModel.getModelPreviousValue();
  }

  public int getPairSliderValue() {
    return commonModel.getModelNumber(true).intValue();
  }

  public double getPairSpinnerValue() {
    return commonModel.getModelNumber(false).doubleValue();
  }

  public void incrementPairValue(Number n) {
    commonModel.incrementModelValue(n);
  }

  public void setPairMaximum(Number n) {
    commonModel.setModelMaximum(n);
  }

  public void setPairMinimum(Number n) {
    commonModel.setModelMinimum(n);
  }

  public void setPairStepSize(Number n) {
    commonModel.setModelStepSize(n);
  }

  public void setPairValue(Number n) {
    commonModel.setModelValue(n);
  }

  public void setFloor(Number n) {
    commonModel.setModelFloor(n);
  }

  public void setCeiling(Number n) {
    commonModel.setModelCeiling(n);
  }

  // The shared model - two delegates controlled from above
  private class NumericModel {
    private NumericModel() {}

    private boolean modelWraps = false;
    private double floor;
    private double ceiling;

    private enum Source {
      SPINNER,
      SLIDER,
      OTHER
    }

    private final EnumSet<Source> sources = EnumSet.noneOf(Source.class);

    // initialise delegates with linked spinner values
    private void setDelegateValues() {
      if (getLinkedSpinner() == null) {
        return;
      }

      SpinnerNumberModel spinModel = (SpinnerNumberModel) getLinkedSpinner().getModel();
      setModelFloor(((Number) spinModel.getMinimum()).doubleValue());
      setModelCeiling(((Number) spinModel.getMaximum()).doubleValue());
      spinnerModelDelegate =
          new NumericSpinnerModel(
              spinModel.getNumber().doubleValue(),
              floor,
              ceiling,
              spinModel.getStepSize().doubleValue());
      getLinkedSpinner().addChangeListener(spinnerEditListener);
      getLinkedSpinner()
          .addMouseWheelListener(e -> incrementModelValue(e.getPreciseWheelRotation()));

      sliderModelDelegate =
          new NumericSliderModel(
              spinnerToSlider.apply(spinnerModelDelegate.getNumber()),
              0,
              spinnerToSlider.apply(floor),
              spinnerToSlider.apply(ceiling));
      getLinkedSlider().addChangeListener(sliderChangeListener);
      getLinkedSlider()
          .addMouseWheelListener(
              e -> {
                sliderModelDelegate.setValue(
                    Math.clamp(
                        sliderModelDelegate.getValue()
                            + (int) Math.round(e.getPreciseWheelRotation()),
                        spinnerToSlider.apply(floor),
                        spinnerToSlider.apply(ceiling)));
              });
    }

    // a method for every flavour
    private void incrementModelValue(Number delta) {
      if (MathUtil.isInt(delta)) {
        setModelValue(getModelNumber(true).intValue() + delta.intValue(), Source.SLIDER);
      } else {
        setModelValue(getModelNumber(false).doubleValue() + delta.doubleValue(), Source.OTHER);
      }
    }

    private void setModelStepSize(Number n) {
      spinnerModelDelegate.setStepSize(n);
    }

    private void setModelValue(Number n) {
      setModelValue(n, Source.OTHER);
    }

    private void setModelValue(Number n, Source s) {
      if (!s.equals(Source.OTHER)
          && sources.contains(Source.SPINNER)
          && sources.contains(Source.SLIDER)) {
        sources.clear();
        return;
      } else {
        sources.add(s);
      }

      double newVal;
      Number currentVal = spinnerModelDelegate.getNumber();
      if (s.equals(Source.SLIDER)) {
        newVal = sliderToSpinner.apply(n.intValue()).doubleValue();
      } else {
        newVal = n.doubleValue();
      }

      if (modelWraps) {
        // wrap if exceeding range
        if (newVal < floor) {
          newVal = newVal - floor + ceiling;
        } else if (newVal >= ceiling) {
          newVal = newVal - ceiling + floor;
        }
      } else {
        // constrain to range
        newVal = Math.clamp(newVal, floor, ceiling);
      }

      setProperty(newVal);

      PropertyChangeEvent pce = new PropertyChangeEvent(this, "value", currentVal, newVal);

      try {
        vcs.fireVetoableChange(pce);
        if (s.equals(Source.OTHER)) {
          sources.add(Source.SPINNER);
          spinnerModelDelegate.setValue(newVal);
        } else if (s.equals(Source.SPINNER) && !sources.contains(Source.SLIDER)) {
          sliderModelDelegate.setValue(spinnerToSlider.apply(newVal));
        } else if (s.equals(Source.SLIDER) && !sources.contains(Source.SPINNER)) {
          spinnerModelDelegate.setValue(newVal);
        } else {
          return;
        }
        pcs.firePropertyChange(pce);
      } catch (PropertyVetoException ignored) {
      }
    }

    private void setModelMaximum(Number n) {
      if (MathUtil.isInt(n)) {
        sliderModelDelegate.setMaximum(n.intValue());
        spinnerModelDelegate.setMaximum(sliderToSpinner.apply(n.intValue()).doubleValue());
      } else {
        sliderModelDelegate.setMaximum(spinnerToSlider.apply(n));
        spinnerModelDelegate.setMaximum(n.doubleValue());
      }
    }

    private void setModelMinimum(Number n) {
      if (MathUtil.isInt(n)) {
        sliderModelDelegate.setMinimum(n.intValue());
        spinnerModelDelegate.setMinimum(sliderToSpinner.apply(n.intValue()).doubleValue());
      } else {
        sliderModelDelegate.setMinimum(spinnerToSlider.apply(n));
        spinnerModelDelegate.setMinimum(n.doubleValue());
      }
    }

    public void setModelCeiling(Number n) {
      ceiling = n.doubleValue();
    }

    public void setModelFloor(Number n) {
      floor = n.doubleValue();
    }

    private Number getModelNextValue() {
      setModelValue(
          getModelNumber(false).doubleValue() + spinnerModelDelegate.getStepSize().doubleValue(),
          Source.OTHER);
      return getModelNumber(false);
    }

    private Number getModelPreviousValue() {
      setModelValue(
          getModelNumber(false).doubleValue() - spinnerModelDelegate.getStepSize().doubleValue(),
          Source.OTHER);
      return getModelNumber(false);
    }

    private Number getModelNumber(boolean asInt) {
      if (asInt) {
        return spinnerToSlider.apply(spinnerModelDelegate.getNumber());
      } else {
        return spinnerModelDelegate.getNumber();
      }
    }

    // Delegate classes with additional setters and normal setters redirected
    private NumericSpinnerModel spinnerModelDelegate = new NumericSpinnerModel(0d, 0d, 100d, 1d);

    private class NumericSpinnerModel extends SpinnerNumberModel {
      private NumericSpinnerModel(double value, double minimum, double maximum, double stepSize) {
        super(value, minimum, maximum, stepSize);
      }

      @Override
      public void setValue(Object value) {
        super.setValue(value);
        setModelValue((Number) value, Source.SPINNER);
      }

      @Override
      public String toString() {
        return "spinnerModelDelegate{"
            + "min="
            + getMinimum()
            + ", max="
            + getMaximum()
            + ", val="
            + getValue()
            + ", stepSize="
            + getStepSize()
            + '}';
      }
    }

    private NumericSliderModel sliderModelDelegate = new NumericSliderModel(0, 0, 0, 100);

    private class NumericSliderModel extends DefaultBoundedRangeModel {
      private NumericSliderModel(int value, int extent, int min, int max) {
        super(value, extent, min, max);
      }

      @Override
      public void setValue(int n) {
        super.setValue(n);
        setModelValue(n, Source.SLIDER);
      }

      @Override
      public String toString() {
        return "sliderModelDelegate{"
            + "min="
            + super.getMinimum()
            + ", max="
            + super.getMaximum()
            + ", val="
            + super.getValue()
            + ", extent="
            + super.getExtent()
            + ", isAdjusting="
            + super.getValueIsAdjusting()
            + '}';
      }
    }

    @Override
    public String toString() {
      return "NumericModel{"
          + spinnerModelDelegate.toString()
          + ", "
          + sliderModelDelegate.toString()
          + '}';
    }

    private final ChangeListener sliderChangeListener =
        e -> {
          // enforces live updates while dragging slider.
          JSlider slider = (JSlider) e.getSource();
          setModelValue(slider.getValue(), Source.SLIDER);
        };
    private final ChangeListener spinnerEditListener =
        e -> {
          JSpinner spinner = (JSpinner) e.getSource();
          try {
            spinner.commitEdit();
          } catch (ParseException pe) {
            // Edited value is invalid, revert the spinner to the last valid value,
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.NumberEditor) {
              ((JSpinner.NumberEditor) editor).getTextField().setValue(spinner.getValue());
            }
          }
        };
  }

  @Override
  public String toString() {
    return "SpinnerSliderPaired{"
        + "spinnerName="
        + getLinkedSpinner().getName()
        + ", sliderName="
        + getLinkedSlider().getName()
        + ", propertySetterSet="
        + (propertySetter != null)
        + ", property="
        + propertyName
        + ", sliderToSpinner(0)="
        + sliderToSpinner.apply(0)
        + ", sliderToSpinner(1)="
        + sliderToSpinner.apply(1)
        + ", spinnerToSlider(0)="
        + spinnerToSlider.apply(0)
        + ", spinnerToSlider(1)="
        + spinnerToSlider.apply(1)
        + ", spinnerToSlider(0)="
        + spinnerToSlider.apply(0)
        + ", sliderToSpinner("
        + spinnerToSlider.apply(0)
        + ")="
        + sliderToSpinner.apply(spinnerToSlider.apply(0))
        + ", spinnerToSlider(1)="
        + spinnerToSlider.apply(1)
        + ", sliderToSpinner("
        + spinnerToSlider.apply(1)
        + ")="
        + sliderToSpinner.apply(spinnerToSlider.apply(1))
        + '}';
  }
}
