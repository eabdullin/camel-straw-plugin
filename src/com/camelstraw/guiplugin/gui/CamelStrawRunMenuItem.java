package com.camelstraw.guiplugin.gui;

import com.camelstraw.CamelStrawUtil;
import com.camelstraw.guiplugin.config.CamelStraw;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class CamelStrawRunMenuItem extends CamelStrawMenuItem {
    private static final Logger log = LoggerFactory.getLogger(CamelStraw.class);
    private String urlBase = "http://camel-straw-stable.azurewebsites.net";
    public CamelStrawRunMenuItem() {
        super(21);
    }

    @Override
    protected String getIconName() {
        return "start";
    }

    @Override
    protected String getToolTip() {
        return "Remote start with CamelStraw";
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        GuiPackage gui = GuiPackage.getInstance();
        JMeterTreeModel model = gui.getTreeModel();
        if(model == null){
            return;
        }
        List<JMeterTreeNode> nodes = model.getNodesOfType(com.camelstraw.guiplugin.config.CamelStraw.class);
        if(nodes == null || nodes.size() <= 0){
            return;
        }
        com.camelstraw.guiplugin.config.CamelStraw auth = (CamelStraw) nodes.get(0).getTestElement();
        if ( popupCheckExistingFileListener(model.getTestPlan()) ) {
            try {

                String filename = gui.getTestPlanFile();
                JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),"");
//                int response = JOptionPane.showOptionDialog(GuiPackage.getInstance().getMainFrame(),
//                        question, JMeterUtils.getResString("warning"),
//                        JOptionPane.YES_NO_CANCEL_OPTION,
//                        JOptionPane.WARNING_MESSAGE,
//                        null,
//                        option,
//                        option[0]);

                Map response = (Map)CamelStrawUtil.sendHttpFile(urlBase + "/api/loadtest?accessToken="+auth.getAccessToken(),filename);
                String url = (String) response.get("url");
                log.info("load test created url: "+url);
                CamelStrawUtil.openBrowser(url);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}

