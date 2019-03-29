package com.camelstraw.statsplugin;

import com.camelstraw.CamelStrawUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CamelStrawStatsListenerProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(CamelStrawStatsListenerProcessor.class);
    private String loadTestId;
    private String host;
    private ConcurrentLinkedQueue<SampleResult> queue;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerHandle;
    private boolean isLastSend = false;
    private static final int MAX_POOL_SIZE = 1;
    private static final int DEFAULT_SEND_INTERVAL = 5;
    private static final String CUMULATED_METRICS = "all";
    private static Object LOCK = new Object();
    private int instanceCount = 0;
    private static CamelStrawStatsListenerProcessor instance  = new CamelStrawStatsListenerProcessor();
    private CamelStrawStatsListenerProcessor() {
    }



    public static void start() {
        synchronized (LOCK){
            if(instance.instanceCount == 0){
                log.info("CamelStrawStatsListenerProcessor starting");
                String loadTestId = JMeterUtils.getProperty("camelstraw.loadTestId");
                if (loadTestId == null || loadTestId == "") {
                    throw new NullPointerException("cannot find camelstraw.loadTestId");
                }

                String host = JMeterUtils.getProperty("camelstraw.host");
                if (host == null || host == "") {
                    throw new NullPointerException("cannot find camelstraw.host");
                }
                String interval = JMeterUtils.getProperty("camelstraw.send_interval");
                int sendInterval;
                try {
                    sendInterval = Integer.parseInt(interval);
                } catch (NumberFormatException nfe) {
                    log.warn("Invalid send interval '{}' defaulting to {}", interval, DEFAULT_SEND_INTERVAL);
                    sendInterval = DEFAULT_SEND_INTERVAL;
                }
                instance.host = host;
                instance.loadTestId = loadTestId;
                instance.scheduler = Executors.newScheduledThreadPool(MAX_POOL_SIZE);
                instance.timerHandle = instance.scheduler.scheduleAtFixedRate(instance, 0L, sendInterval, TimeUnit.SECONDS);
                instance.queue = new ConcurrentLinkedQueue<>();
                instance.isLastSend = false;
                instance.instanceCount++;
            }else{
                log.info("CamelStrawStatsListenerProcessor already started");
                instance.instanceCount++;
            }
        }
    }
    public static void addSampleResult(SampleResult sampleResult){
        if(instance.instanceCount == 0){
            log.info("Adding metric unsuccessful. CamelStrawStatsListenerProcessor not started");
            return;
        }
        instance.queue.add(sampleResult);
    }
    public static void shutDown() {
        synchronized (LOCK){
            if(instance.instanceCount > 0){
                instance.instanceCount--;
                if(instance.instanceCount <= 0){
                    log.info("stopping CamelStrawStatsListenerProcessor: {}");
                    boolean cancelState = instance.timerHandle.cancel(false);
                    log.debug("Canceled state: {}", cancelState);
                    instance.scheduler.shutdown();
                    try {
                        if(!instance.scheduler.isTerminated()){
                            instance.scheduler.awaitTermination(5, TimeUnit.SECONDS);
                        }

                    } catch (InterruptedException e) {
                        log.error("Error waiting for end of scheduler");
                    }
                    instance.isLastSend = true;
                    instance.run(); // last run
                }
            }else{
                log.info("CamelStrawStatsListenerProcessor already stopped");
            }

        }

    }

    @Override
    public void run() {
        log.info("CamelStraw: start calculating metrics");

        HashMap<String, CamelStrawSamplerMetric> metricsPerSampler = new HashMap();
        metricsPerSampler.put(CUMULATED_METRICS, new CamelStrawSamplerMetric());
        UserMetric userMetric = new UserMetric();
        if (queue.isEmpty()) {
            log.info("stats queue is empty");
            return;
        }
        while (!queue.isEmpty()) {
            SampleResult sr = queue.poll();

            userMetric.add(sr);
            String samplerName = sr.getSampleLabel();
            if (!metricsPerSampler.containsKey(samplerName)) {
                metricsPerSampler.put(samplerName, new CamelStrawSamplerMetric());
            }
            metricsPerSampler.get(samplerName).add(sr);
            metricsPerSampler.get(CUMULATED_METRICS).add(sr);
        }
        long timestampInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        log.info("CamelStraw: start sending metrics");
        int totalLength = metricsPerSampler.get(CUMULATED_METRICS).getTotal();
        List<Map<String, Object>> dataArray = new ArrayList<>();
        for (Map.Entry<String, CamelStrawSamplerMetric> entry : metricsPerSampler.entrySet()) {
            final String key = entry.getKey();
            final CamelStrawSamplerMetric metric = entry.getValue();
            String contextName = StringUtils.replaceChars(key, "\\ .", "--_");
            if (metric.getTotal() > 0) {
                Map<String, Object> data = metric.toMap(timestampInSeconds, contextName, userMetric, isLastSend);
                dataArray.add(data);
            }
        }

        if (dataArray.size() > 0) {

            String url = host + "/api/loadtest/" + loadTestId + "/stats/samples";
            CamelStrawUtil.sendHttpRequest(url, "POST", dataArray);
            log.info("CamelStraw: {} samples metircs sent", totalLength);
        }
    }

}
