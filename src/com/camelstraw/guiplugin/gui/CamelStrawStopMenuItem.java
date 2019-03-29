package com.camelstraw.guiplugin.gui;

import java.awt.event.ActionEvent;

public class CamelStrawStopMenuItem extends CamelStrawMenuItem {

    public CamelStrawStopMenuItem() {
        super(22);
    }

    @Override
    protected String getIconName() {
        return "stop";
    }

    @Override
    protected String getToolTip() {
        return "Remote start with CamelStraw";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
