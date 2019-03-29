package com.camelstraw.guiplugin.config;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.visualizers.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class CamelStraw extends ConfigTestElement implements Backend, Serializable, NoThreadClone, Remoteable {
    private static final Logger log = LoggerFactory.getLogger(CamelStraw.class);
    public static final String ACCESSTOKEN = "access_token";
    public static final String SLAVESNUMBER = "slaves_number";

    public CamelStraw() {

    }

    public String getAccessToken(){
        log.debug("Return access token: " + getPropertyAsString(ACCESSTOKEN));
        return getPropertyAsString(ACCESSTOKEN);
    }
//    public int getSlavesNumber(){
//        log.debug("Return salves: " + getPropertyAsString(SLAVESNUMBER));
//        return getPropertyAsInt(SLAVESNUMBER);
//    }

    public void setAccessToken(String accessToken) {
        log.debug("Set access token to: " + accessToken);
        setProperty(ACCESSTOKEN, accessToken);
    }
//    public void setSlavesNumber(int number) {
//        log.debug("Set slaves to: " + number);
//        setProperty(SLAVESNUMBER, number);
//    }

}
