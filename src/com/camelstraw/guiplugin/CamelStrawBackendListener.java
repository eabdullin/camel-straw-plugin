package com.camelstraw.guiplugin;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.apache.jmeter.visualizers.backend.SamplerMetric;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class CamelStrawBackendListener extends AbstractBackendListenerClient implements Runnable  {
    private static final Logger log = LoggerFactory.getLogger(CamelStrawBackendListener.class);
    private String loadTestId = null;
    private String host = null;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerHandle;
    private static final int MAX_POOL_SIZE = 1;
    private static final long SEND_INTERVAL = 5;
    private static boolean isLastSend = false;
    String[]  percentilesStringArray = new String[]{"90", "95", "99"};
    private Map<String, Object> getMap(long timestampInSeconds, String contextName, SamplerMetric metric, UserMetric userMetric) {
        Map<String, Object> map = new HashMap<String, Object>();


        Map<String,Object> allResponses = new HashMap<>();
        //all responses
        allResponses.put("count", metric.getTotal());
        allResponses.put("min",metric.getAllMinTime());
        allResponses.put("max",metric.getAllMaxTime());
        allResponses.put("avg",metric.getAllMean());
        Map<String, Double>  percentileAll = new HashMap<>();
        for (String p : percentilesStringArray) {
            Float percentileValue = Float.valueOf(p.trim());
            percentileAll.put( p,metric.getAllPercentile(percentileValue));
        }
        allResponses.put("pct",percentileAll);
        map.put("allResponses",allResponses);
        Map<String,Object> successfulResponses =  new HashMap<String, Object>();
        successfulResponses.put("count", metric.getSuccesses());
        //success
        if(metric.getSuccesses()>0) {
            successfulResponses.put("min",metric.getOkMinTime());
            successfulResponses.put("max",metric.getOkMaxTime());
            successfulResponses.put("avg",metric.getOkMean());
            Map<String, Double>  percentile = new HashMap<>();
            for (String p : percentilesStringArray) {
                Float percentileValue = Float.valueOf(p.trim());
                percentile.put( p,metric.getOkPercentile(percentileValue));
            }
            successfulResponses.put("pct",percentile);
        }
        map.put("successfulResponses",successfulResponses);
        Map<String,Object> failedResponses =  new HashMap<String, Object>();
        failedResponses.put("count", metric.getFailures());

        //failed
        if(metric.getFailures()>0) {
            failedResponses.put("min",metric.getKoMinTime());
            failedResponses.put("max",metric.getKoMaxTime());
            failedResponses.put("avg",metric.getKoMean());
            Map<String, Double>  percentile = new HashMap<>();
            for (String p : percentilesStringArray) {
                Float percentileValue = Float.valueOf(p.trim());
                percentile.put( p,metric.getKoPercentile(percentileValue));
            }
            failedResponses.put("pct",percentile);
        }
        map.put("failedResponses",failedResponses);



        map.put("hitsCount",metric.getHits());
        map.put("receivedBytes",metric.getReceivedBytes());
        map.put("sentBytes",metric.getSentBytes());
        map.put("isLast",isLastSend);
        map.put("sampleName", contextName);
        map.put("createdAt", timestampInSeconds);


        map.put("minActiveThreads", userMetric.getMinActiveThreads());
        map.put("maxActiveThreads", userMetric.getMaxActiveThreads());
        map.put("meanActiveThreads", userMetric.getMeanActiveThreads());
        map.put("finishedThreads", userMetric.getFinishedThreads());
        map.put("startedThreads", userMetric.getStartedThreads());
        return map;
    }
    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        boolean samplersToFilterMatch;
        synchronized (LOCK) {
            UserMetric userMetrics = getUserMetrics();
            for (SampleResult sampleResult : sampleResults) {

                userMetrics.add(sampleResult);
                SamplerMetric samplerMetric = getSamplerMetric(sampleResult.getSampleLabel());
                samplerMetric.add(sampleResult);
                SamplerMetric cumulatedMetrics = getSamplerMetric(CUMULATED_METRICS);
                cumulatedMetrics.add(sampleResult);
            }
        }
    }
    protected void sendMetrics() {
        long timestampInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        log.info("CamelStraw: start sending metrics");

        synchronized (LOCK) {
            UserMetric userMetric = getUserMetrics();
            Set<Map.Entry<String, SamplerMetric>> set = getMetricsPerSampler().entrySet();
            List<Map<String, Object>> dataArray = new ArrayList<>();
            for (Map.Entry<String, SamplerMetric> entry : set) {
                final String key = entry.getKey();
                final SamplerMetric metric = entry.getValue();
                String contextName = StringUtils.replaceChars(key, "\\ .", "--_");
                if(metric.getTotal() > 0){
                    Map<String, Object> data = getMap(timestampInSeconds, contextName, metric,userMetric);
                    dataArray.add(data);
                }
                // We are computing on interval basis so cleanup7
                metric.resetForTimeInterval();
            }
            if(dataArray.size() > 0){

//                String url = host + "/api/loadtest/" + loadTestId + "/stats/samples";
//                CamelStrawUtil.sendHttpRequest(url,"POST", dataArray);
                log.info("CamelStraw: metircs sended");
            }
        }
    }

    private static final String CUMULATED_METRICS = "all";
    private static final Object LOCK = new Object();

    @Override
    public void run() {
        sendMetrics();
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        log.info("CamelStraw: starting backend listener");
        loadTestId = JMeterUtils.getProperty("camelstraw.loadTestId");

        if(loadTestId == null || loadTestId == ""){
            throw new NullPointerException("cannot find camelstraw.loadTestId");
        }
        host = JMeterUtils.getProperty("camelstraw.host");
        if(host == null || host == ""){
            throw new NullPointerException("cannot find camelstraw.host");
        }
        scheduler = Executors.newScheduledThreadPool(MAX_POOL_SIZE);
        // Don't change this as metrics are per second
        this.timerHandle = scheduler.scheduleAtFixedRate(this, 0L, SEND_INTERVAL, TimeUnit.SECONDS);
    }
    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        log.info("CamelStraw: stopping backend listener");
        boolean cancelState = timerHandle.cancel(false);
        log.debug("Canceled state: {}", cancelState);
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Error waiting for end of scheduler");
            Thread.currentThread().interrupt();
        }
        // Send last set of data before ending
        isLastSend = true;
        sendMetrics();

        super.teardownTest(context);
    }
    static String sanitizeString(String s) {
        // String#replace uses regexp
        return StringUtils.replaceChars(s, "\\ .", "--_");
    }
}
