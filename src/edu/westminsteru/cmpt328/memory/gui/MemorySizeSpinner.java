package edu.westminsteru.cmpt328.memory.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.text.ParseException;

public class MemorySizeSpinner extends JSpinner {

    private static class Model extends AbstractSpinnerModel {

        private MemorySize value;
        private MemorySize minimum, maximum;

        public Model(MemorySize value) {
            this(value, MemorySize.of(1, Unit.Byte), MemorySize.of(1023, Unit.Yobibyte));
        }

        public Model(MemorySize value, MemorySize minimum, MemorySize maximum) {
            if (value == null)
                throw new IllegalArgumentException("value cannot be null!");
            else if (minimum == null)
                throw new IllegalArgumentException("minimum cannot be null!");
            else if (maximum == null)
                throw new IllegalArgumentException("maximum cannot be null!");
            else if (value.compareTo(minimum) < 0)
                throw new IllegalArgumentException(String.format("value cannot be less than minimum! (value: %s; minimum: %s)", value, minimum));
            else if (value.compareTo(maximum) > 0)
                throw new IllegalArgumentException(String.format("value cannot be greater than maximum! (value: %s; maximum: %s)", value, maximum));

            this.value = value;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public MemorySize getValue() {
            return value;
        }

        @Override
        public void setValue(Object o) {
            if (o instanceof MemorySize newValue) {
                var previousValue = this.value;
                this.value = MemorySize.min(maximum, MemorySize.max(minimum, newValue));

                if (!this.value.equals(previousValue))
                    fireStateChanged();
            }
        }

        @Override
        public MemorySize getNextValue() {
            return MemorySize.of(value.count() * 2, value.unit());
        }

        @Override
        public MemorySize getPreviousValue() {
            if (value.equals(MemorySize.of(1, Unit.Byte)))
                return null;
            else if (value.unit() == Unit.Byte)
                return MemorySize.of(value.count() / 2, Unit.Byte);
            else {
                Unit prevUnit = Unit.values()[value.unit().ordinal() - 1];
                return MemorySize.of(512 * value.count(), prevUnit);
            }
        }

        void setMinimum(MemorySize minimum) {
            if (minimum == null)
                throw new IllegalArgumentException("minimum cannot be null!");
            this.minimum = minimum;
            setValue(value);
        }

        void setMaximum(MemorySize maximum) {
            if (maximum == null)
                throw new IllegalArgumentException("maximum cannot be null!");
            this.maximum = maximum;
            setValue(value);
        }
    }

    private class Editor extends JSpinner.DefaultEditor {

        public Editor() {
            super(MemorySizeSpinner.this);
            var field = getTextField();
            field.setHorizontalAlignment(JFormattedTextField.CENTER);
            field.setEditable(true);
            getTextField().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    validate();
                }

                private void validate() {
                    String text = getTextField().getText();
                    if (MemorySize.parse(text).isPresent())
                        getTextField().setForeground(getTextField().getParent().getForeground());
                    else
                        getTextField().setForeground(Color.RED);
                }
            });

            getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        commitEdit();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            getTextField().setText(model.getValue().toString());
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("value")) {
                var maybeSize = MemorySize.parse(e.getNewValue().toString());
                maybeSize.ifPresent(model::setValue);
            }
        }
    }

    private Model model;

    public MemorySizeSpinner(MemorySize value) {
        model = new Model(value);
        setModel(model);
        setEditor(new Editor());
    }

    public void setMinimum(MemorySize minimum) {
        model.setMinimum(minimum);
    }

    public void setMaximum(MemorySize maximum) {
        model.setMaximum(maximum);
    }

    public MemorySize getValue() {
        if (model == null)
            return null;
        return model.getValue();
    }
}
