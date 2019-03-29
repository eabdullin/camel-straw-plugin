package com.camelstraw.statsplugin;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelStrawStatsListenerGui extends AbstractConfigGui {
    private static final Logger log = LoggerFactory.getLogger(CamelStrawStatsListenerGui.class);
    @Override
    public String getLabelResource() {
        return "Camel Straw Stats";
    }

    @Override
    public TestElement createTestElement() {
        log.debug("[Camel Straw Stats plugin] createTestElement");
        CamelStrawStatsListener listener = new CamelStrawStatsListener();
        return listener;
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        log.debug("[Camel Straw Stats plugin] modifyTestElement");
        configureTestElement(testElement);
    }
}
