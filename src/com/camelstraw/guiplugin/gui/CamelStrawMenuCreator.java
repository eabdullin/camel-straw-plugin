package com.camelstraw.guiplugin.gui;

import org.apache.jmeter.gui.plugin.MenuCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

public class CamelStrawMenuCreator implements MenuCreator {
    private static final Logger log = LoggerFactory.getLogger(CamelStrawMenuCreator.class);

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION menu_location) {
        if(menu_location == MENU_LOCATION.RUN){
            try{
                return new JMenuItem[]{new CamelStrawRunMenuItem()};
            }catch (Throwable e){
                log.error("Failed to load camel straw", e);
            }
        }
        return new JMenuItem[0];
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menuElement) {
        return false;
    }

    @Override
    public void localeChanged() {

    }
}
