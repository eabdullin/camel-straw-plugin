package com.camelstraw.guiplugin.config;

import com.camelstraw.guiplugin.gui.CamelStrawMenuItem;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class CamelStrawGui extends AbstractConfigGui {
    private static final Logger log = LoggerFactory.getLogger(CamelStrawMenuItem.class);
    private JTextField tfAccessToken;

    public CamelStrawGui(){
        super();
        init();
        initFields();
    }
    @Override
    public String getLabelResource() {
        return "Camel Straw";
    }

    @Override
    public TestElement createTestElement() {
        log.debug("[CamelStraw plugin] createTestElement");
        com.camelstraw.guiplugin.config.CamelStraw auth = new com.camelstraw.guiplugin.config.CamelStraw();
        //modifyTestElement(auth);
        return auth;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        log.debug("[CamelStraw plugin] modifyTestElement");
        configureTestElement(element);
        com.camelstraw.guiplugin.config.CamelStraw lf = (com.camelstraw.guiplugin.config.CamelStraw) element;
        lf.setAccessToken(tfAccessToken.getText());

//        try {
//            int value = Integer.parseInt(tfSlavesNumber.getText());
//            lf.setSlavesNumber(value);
//        } catch (NumberFormatException | NullPointerException nfe) {
//            tfSlavesNumber.setText("");
//            JOptionPane.showMessageDialog(null,
//                    "Error: Please enter number bigger than 0", "Error Massage",
//                    JOptionPane.ERROR_MESSAGE);
//            log.debug("[CamelStraw plugin] cannot parse integer");
//        }

    }


    @Override
    public void configure(TestElement te) {
        log.debug("[CamelStraw plugin] configure");
        super.configure(te);
        com.camelstraw.guiplugin.config.CamelStraw lf = (CamelStraw) te;
        tfAccessToken.setText(lf.getAccessToken());
//        tfSlavesNumber.setText(String.valueOf(lf.getSlavesNumber()));
    }
    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }
    private void init() {
        log.debug("[CamelStraw plugin] init");
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        editConstraints.insets = new Insets(2, 0, 0, 0);
        labelConstraints.insets = new Insets(2, 0, 0, 0);

        addToPanel(mainPanel, labelConstraints, 0, 0, new JLabel("Access Token: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, 0, tfAccessToken = new JTextField(20));

        JPanel container = new JPanel(new BorderLayout());
        container.add(mainPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
    }

    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }

    private void initFields() {
        log.debug("[CamelStraw plugin] initFields");
        tfAccessToken.setText("");
    }
}
