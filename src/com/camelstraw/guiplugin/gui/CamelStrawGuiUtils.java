package com.camelstraw.guiplugin.gui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
public class CamelStrawGuiUtils {
    private CamelStrawGuiUtils(){}
    private static final Logger log = LoggerFactory.getLogger(CamelStrawGuiUtils.class);

    public static <T extends Component> T findComponentIn(Container container,  Class<T> search) {
        log.debug("Searching in " + container);
        for (Component a : container.getComponents()) {
            if (search.isAssignableFrom(a.getClass())) {
                log.debug("Found " + a);
                return (T) a;
            }

            if (a instanceof Container) {
                T res = findComponentIn((Container) a,search);
                if (res != null) {
                    return res;
                }
            }
        }

        return null;
    }
}
