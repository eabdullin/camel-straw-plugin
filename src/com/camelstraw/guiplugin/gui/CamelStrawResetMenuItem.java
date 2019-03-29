package com.camelstraw.guiplugin.gui;

import java.awt.event.ActionEvent;

public class CamelStrawResetMenuItem extends CamelStrawMenuItem  {

    public CamelStrawResetMenuItem() {
        super(23);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    protected String getIconName() {
        return null;
    }

    @Override
    protected String getToolTip() {
        return "Reset CamelStraw test settings";
    }
}
