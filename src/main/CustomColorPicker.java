package main;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class CustomColorPicker extends JPanel {

    private final JSlider redSlider, greenSlider, blueSlider;
    public final JLabel previewColor;

    public CustomColorPicker(Color previousColor) {
        super(new GridBagLayout());

        redSlider = createColorSlider(previousColor.getRed(), "R", new Color(previousColor.getRed(), 0, 0));
        greenSlider = createColorSlider(previousColor.getGreen(), "G", new Color(0, previousColor.getGreen(), 0));
        blueSlider = createColorSlider(previousColor.getBlue(), "B", new Color(0, 0, previousColor.getBlue()));

        JPanel colorPreview = new JPanel(new GridLayout(2, 1));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel previousColorLabel = new JLabel();
        previousColorLabel.setOpaque(true);
        previousColorLabel.setPreferredSize(new Dimension(50, 80));
        previousColorLabel.setBackground(previousColor);

        previewColor = new JLabel();
        previewColor.setOpaque(true);
        previewColor.setPreferredSize(new Dimension(50, 80));
        previewColor.setBackground(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()));

        colorPreview.add(previousColorLabel);
        colorPreview.add(previewColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);

        add(redSlider, gbc);
        add(greenSlider, gbc);
        add(blueSlider, gbc);
        add(colorPreview, gbc);

        ChangeListener sliderListener = e -> {
            previewColor.setBackground(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()));
            redSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(redSlider.getValue(), 0, 0), 2), "R"));
            greenSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, greenSlider.getValue(), 0), 2), "G"));
            blueSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, blueSlider.getValue()), 2), "B"));
        };

        redSlider.addChangeListener(sliderListener);
        greenSlider.addChangeListener(sliderListener);
        blueSlider.addChangeListener(sliderListener);
    }

    private JSlider createColorSlider(int value, String title, Color borderColor) {
        JSlider slider = new JSlider(JSlider.VERTICAL, 0, 255, value);
        slider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColor, 2), title));
        return slider;
    }
}