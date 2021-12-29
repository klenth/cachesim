package edu.westminstercollege.cmpt328.memory.gui;

import edu.westminstercollege.cmpt328.memory.MemorySystem;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class MemorySystemConfigurationTool extends JPanel {

    private class MemoryButton extends JToggleButton {

        private static final int BASE_PREFERRED_WIDTH = 500;

        int level = 0;
        MemorySize size;
        JLabel memoryNameLabel = new JLabel();
        JLabel sizeLabel = new JLabel();

        MemoryButton(int level, MemorySize size) {
            var memoryNameFont = memoryNameLabel.getFont();
            memoryNameLabel.setHorizontalAlignment(JLabel.CENTER);
            memoryNameLabel.setFont(memoryNameFont.deriveFont(memoryNameFont.getSize2D() * 1.25f));
            memoryNameLabel.setAlignmentX(0.5f);
            sizeLabel.setAlignmentX(0.5f);
            setLevel(level);
            setSize(size);
            setAlignmentX(0.5f);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(Box.createVerticalGlue());
            add(Box.createVerticalStrut(10));
            add(memoryNameLabel);
            add(Box.createVerticalStrut(10));
            add(sizeLabel);
            add(Box.createVerticalStrut(10));
            add(Box.createVerticalGlue());
        }

        void setLevel(int level) {
            if (level < 0)
                throw new IllegalArgumentException("Level cannot be negative");
            this.level = level;

            if (level == 0) {
                setFont(getFont().deriveFont(24f));
                memoryNameLabel.setFont(memoryNameLabel.getFont().deriveFont(24f));
                sizeLabel.setFont(sizeLabel.getFont().deriveFont(20f));
                memoryNameLabel.setText("RAM");
            } else {
                memoryNameLabel.setFont(memoryNameLabel.getFont().deriveFont(20f));
                sizeLabel.setFont(sizeLabel.getFont().deriveFont(16f));
                memoryNameLabel.setText("L" + level);
            }
        }

        void setSize(MemorySize size) {
            if (size == null)
                throw new IllegalArgumentException("Size cannot be null");
            this.size = size;
            sizeLabel.setText(size.toString());
        }

        @Override
        public Dimension getPreferredSize() {
            var superPrefSize = super.getPreferredSize();
            var p = new Dimension(superPrefSize);
            var parentSize = getParent().getSize();

            p.width = switch (level) {
                case 0      -> BASE_PREFERRED_WIDTH;
                default     -> (int)(Math.pow(0.7, level - 1) * BASE_PREFERRED_WIDTH / 2);
            };
            if (level == 0)
                p.height *= 2;

            p.width = Math.max(p.width, superPrefSize.width);
            p.height = Math.max(p.height, superPrefSize.height);

            System.out.printf("%s preferred size: %s (parent size: %s)\n", memoryNameLabel.getText(), p, parentSize);

            return p;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    private JPanel memorySystemBox = new JPanel();
    private MemoryButton ramButton = new MemoryButton(0, MemorySize.of(64, Unit.Mebibyte));
    private List<MemoryButton> memoryButtons = new ArrayList<>();

    // For when shown as a dialog
    private boolean confirmed = false;

    public MemorySystemConfigurationTool() {
        setLayout(new BorderLayout(8, 8));
        memorySystemBox.setLayout(new GridBagLayout());
        add(new JScrollPane(memorySystemBox), BorderLayout.CENTER);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Memory system"
        ));

        memoryButtons.add(ramButton);

        memoryButtons.addAll(List.of(
                ramButton,
                new MemoryButton(1,  MemorySize.of(64, Unit.Kibibyte)),
                new MemoryButton(2, MemorySize.of(16, Unit.Kibibyte)),
                new MemoryButton(3, MemorySize.of(4, Unit.Kibibyte))
        ));

        ButtonGroup memoryButtonGroup = new ButtonGroup();

        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        for (int i = memoryButtons.size() - 1; i >= 0; --i) {
            var button = memoryButtons.get(i);
            memorySystemBox.add(button, gbc);
            memoryButtonGroup.add(button);
        }

        ramButton.setSelected(true);

        enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
    }

    @Override
    protected void processComponentEvent(ComponentEvent e) {
        if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
            System.out.println("Resized");
            invalidate();
            memorySystemBox.invalidate();
            validate();
        }
    }

    public boolean showDialog(Frame owner, String title) {
        var dialog = new JDialog(owner, title, true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(this, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new FlowLayout(FlowLayout.TRAILING, 8, 8));
        var cancelButton = new JButton("Cancel");
        var okButton = new JButton("OK");
        south.add(cancelButton);
        south.add(okButton);
        getRootPane().setDefaultButton(okButton);
        add(south, BorderLayout.SOUTH);

        confirmed = false;
        cancelButton.addActionListener(e -> dialog.dispose());
        okButton.addActionListener(e -> {
            confirmed = true;
            dialog.dispose();
        });
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.pack();
        dialog.setVisible(true);

        return confirmed;
    }

    public static void main(String... args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        var tool = new MemorySystemConfigurationTool();
        if (tool.showDialog(null, "Configure memory system")) {
            System.out.println("Confirmed");
        } else
            System.out.println("Cancelled");

    }
}
