package com.camelstraw.statsplugin;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.visualizers.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class CamelStrawStatsListener extends ConfigTestElement implements Backend, Serializable, SampleListener, TestStateListener, NoThreadClone, Remoteable {
    private static final Logger log = LoggerFactory.getLogger(CamelStrawStatsListener.class);
    private String whoAmI() {
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().getName());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        sb.append("-");
        sb.append(getName());
        return sb.toString();
    }
    @Override
    public void sampleOccurred(SampleEvent sampleEvent) {
        try {
            SampleResult sr = sampleEvent.getResult();
            CamelStrawStatsListenerProcessor.addSampleResult(sr);
            log.info("{}: added sampleResult, for sampler {}",whoAmI(),sr.getSampleLabel());
        } catch (Exception err) {
            log.error("sampleOccurred, failed to queue the sample", err);
        }
    }

    @Override
    public void sampleStarted(SampleEvent sampleEvent) {
        //nothing
    }

    @Override
    public void sampleStopped(SampleEvent sampleEvent) {
        //nothing
    }

    @Override
    public void testStarted() {
        testStarted("local");
    }

    @Override
    public void testStarted(String s) {
        log.info("{}: test started",whoAmI());
        CamelStrawStatsListenerProcessor.start();
    }

    @Override
    public void testEnded() {
        testEnded("local");
    }

    @Override
    public void testEnded(String s) {
        log.info("{}: test ended",whoAmI());
        CamelStrawStatsListenerProcessor.shutDown();
    }

}
