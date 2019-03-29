package com.camelstraw.guiplugin.gui;
import org.apache.jmeter.engine.DistributedRunner;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

public abstract class CamelStrawMenuItem extends JMenuItem implements ActionListener {
    private static final Logger log = LoggerFactory.getLogger(CamelStrawMenuItem.class);
    private DistributedRunner distributedRunner = new DistributedRunner();
    public CamelStrawMenuItem(int pos){

        addActionListener(this);
        addToolbarIcon(pos);
    }

    protected void addToolbarIcon(int pos) {
        GuiPackage instance = GuiPackage.getInstance();
        if (instance != null) {
            final MainFrame mf = instance.getMainFrame();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JMeterToolBar toolbar = null;
                    while (toolbar == null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            log.debug("Did not add btn to toolbar", e);
                        }
                        log.debug("Searching for toolbar");
                        toolbar = CamelStrawGuiUtils.findComponentIn(mf,JMeterToolBar.class);

                    }
                    Component toolbarButton = getToolbarButton();
                    toolbarButton.setSize(toolbar.getComponent(pos).getSize());
                    toolbar.add(toolbarButton, pos);
                }
            });
        }
    }
    /**
     * @param tree where check if listener has existing file
     * @return true if continue test, false otherwise
     */
    protected boolean popupCheckExistingFileListener(HashTree tree) {

        SearchByClass<ResultCollector> resultListeners = new SearchByClass<>(ResultCollector.class);
        tree.traverse(resultListeners);
        for (ResultCollector rc : resultListeners.getSearchResults()) {
            File f = new File(rc.getFilename());
            if (f.exists()) {
                    String[] option = new String[]{JMeterUtils.getResString("concat_result"),
                            JMeterUtils.getResString("dont_start"), JMeterUtils.getResString("replace_file")};
                    String question = MessageFormat.format(
                            JMeterUtils.getResString("ask_existing_file"), // $NON-NLS-1$
                            rc.getFilename());
                    // Interactive question
                    int response = JOptionPane.showOptionDialog(GuiPackage.getInstance().getMainFrame(),
                            question, JMeterUtils.getResString("warning"),
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            option,
                            option[0]);

                    switch (response) {
                        case JOptionPane.CANCEL_OPTION:
                            // replace_file so delete the existing one
                            if (f.delete()) {
                                break;
                            } else {
                                log.error("Could not delete existing file {}", f.getAbsolutePath());
                                return false;
                            }
                        case JOptionPane.YES_OPTION:
                            // append is the default behaviour, so nothing to do
                            break;
                        case JOptionPane.NO_OPTION:
                        default:
                            // Exit without start the test
                            return false;
                }
            }
        }
        return true;
    }

    protected Component getToolbarButton() {
//        URL url = getClass().getClassLoader().getResource(getIconName());
//        ImageIcon icon = new ImageIcon(url);
        JButton button = new JButton(getIconName());
        button.setToolTipText(getToolTip());
        //button.setPressedIcon(new ImageIcon(imageURLPressed));
        button.addActionListener(this);
        //button.setActionCommand(iconBean.getActionNameResolve());
        return button;
    }

    protected abstract String getIconName();

    protected abstract String getToolTip();

}
