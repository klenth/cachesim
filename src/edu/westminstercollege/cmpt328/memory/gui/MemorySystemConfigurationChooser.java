package edu.westminstercollege.cmpt328.memory.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;

public class MemorySystemConfigurationChooser extends JPanel {

    private static class Switch {
        private boolean value = false;

        void set(boolean value) {
            this.value = value;
        }

        boolean get() {
            return value;
        }
    }

    private MemorySystemTable table = new MemorySystemTable();
    private JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    private JFileChooser fileChooser;

    public MemorySystemConfigurationChooser() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setLayout(new BorderLayout(5, 5));
        add(new JScrollPane(table), BorderLayout.CENTER);

        Icon addIcon = new ImageIcon(getClass().getResource("/icon/bx-plus.png"));
        Icon removeIcon = new ImageIcon(getClass().getResource("/icon/bx-trash.png"));
        Icon loadIcon = new ImageIcon(getClass().getResource("/icon/bx-folder-open.png"));
        Icon saveIcon = new ImageIcon(getClass().getResource("/icon/bx-save.png"));

        toolbar.add(new AbstractAction("Add memory", addIcon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = Math.max(0, table.getSelectedRow());
                table.getModel().insertCacheAt(selectedIndex);
            }
        });

        toolbar.add(new AbstractAction("Remove memory", removeIcon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0)
                    table.getModel().removeCacheAt(selectedRow);
            }
        });

        toolbar.addSeparator();

        toolbar.add(new AbstractAction("Load configuration", loadIcon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    var chooser = getConfigurationFileChooser();
                    if (chooser.showOpenDialog(MemorySystemConfigurationChooser.this) == JFileChooser.APPROVE_OPTION) {
                        Path selected = chooser.getSelectedFile().toPath();

                        var config = MemorySystemConfiguration.loadJson(selected);
                        table.getModel().setConfiguration(config);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            MemorySystemConfigurationChooser.this,
                            "Input/output error while reading configuration file:\n" + ex.getLocalizedMessage(),
                            "I/O error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } catch (MemorySystemConfiguration.InvalidConfigurationException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            MemorySystemConfigurationChooser.this,
                            "Invalid memory system configuration:\n" + ex.getLocalizedMessage(),
                            "Invalid configuration file",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        toolbar.add(new AbstractAction("Save configuration", saveIcon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                var fileChooser = getConfigurationFileChooser();
                if (fileChooser.showSaveDialog(MemorySystemConfigurationChooser.this) == JFileChooser.APPROVE_OPTION) {
                    var selected = fileChooser.getSelectedFile().toPath();
                    var config = table.getModel().getConfiguration();
                    try {
                        config.saveJson(selected);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                MemorySystemConfigurationChooser.this,
                                "Input/output error while saving configuration file:\n" + ex.getLocalizedMessage(),
                                "I/O error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });

        add(toolbar, BorderLayout.EAST);
    }

    public boolean showDialog(JFrame owner) {
        JDialog dialog = new JDialog(owner, "Configure memory system", true);
        dialog.setLayout(new BorderLayout(5, 5));
        ((JComponent)dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dialog.add(this, BorderLayout.CENTER);

        Box buttons = new Box(BoxLayout.LINE_AXIS);
        JButton cancelButton = new JButton("Cancel"),
                okButton = new JButton("OK");
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(cancelButton);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(okButton);
        buttons.add(Box.createHorizontalStrut(5));
        dialog.add(buttons, BorderLayout.AFTER_LAST_LINE);

        var accepted = new Switch();

        cancelButton.addActionListener(e -> {
            accepted.set(false);
            dialog.dispose();
        });

        okButton.addActionListener(e -> {
            accepted.set(true);
            dialog.dispose();
        });

        dialog.getRootPane().setDefaultButton(okButton);
        dialog.pack();
        dialog.setVisible(true);

        return accepted.get();
    }

    public void setConfiguration(MemorySystemConfiguration configuration) {
        table.getModel().setConfiguration(configuration);
    }

    public MemorySystemConfiguration getConfiguration() {
        return table.getModel().getConfiguration();
    }

    private JFileChooser getConfigurationFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
            fileChooser.setAcceptAllFileFilterUsed(true);
        }

        return fileChooser;
    }

    public static void main(String... args) {
        var viewer = new MemorySystemConfigurationChooser();
        viewer.showDialog(null);
        System.exit(0);
    }
}
