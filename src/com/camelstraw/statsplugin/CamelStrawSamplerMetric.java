package com.camelstraw.statsplugin;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.SamplerMetric;
import org.apache.jmeter.visualizers.backend.UserMetric;

import java.util.HashMap;
import java.util.Map;

public class CamelStrawSamplerMetric extends SamplerMetric {
    private long latency;
    private long connectTime;
    private long startTime = -1;
    private long endTime = -1;
    public Map<String, Object> toMap(long timestampInSeconds, String contextName, UserMetric userMetric, boolean isLastSend){
        String[]  percentilesStringArray = new String[]{"90", "95", "99"};
        Map<String, Object> map = new HashMap<String, Object>();


        Map<String,Object> allResponses = new HashMap<>();
        //all responses
        allResponses.put("count", getTotal());
        allResponses.put("min",getAllMinTime());
        allResponses.put("max",getAllMaxTime());
        allResponses.put("avg",getAllMean());
        Map<String, Double>  percentileAll = new HashMap<>();
        for (String p : percentilesStringArray) {
            Float percentileValue = Float.valueOf(p.trim());
            percentileAll.put( p,getAllPercentile(percentileValue));
        }
        allResponses.put("pct",percentileAll);
        map.put("allResponses",allResponses);
        Map<String,Object> successfulResponses =  new HashMap<String, Object>();
        successfulResponses.put("count", getSuccesses());
        //success
        if(getSuccesses()>0) {
            successfulResponses.put("min",getOkMinTime());
            successfulResponses.put("max",getOkMaxTime());
            successfulResponses.put("avg",getOkMean());
            Map<String, Double>  percentile = new HashMap<>();
            for (String p : percentilesStringArray) {
                Float percentileValue = Float.valueOf(p.trim());
                percentile.put( p,getOkPercentile(percentileValue));
            }
            successfulResponses.put("pct",percentile);
        }
        map.put("successfulResponses",successfulResponses);
        Map<String,Object> failedResponses =  new HashMap<String, Object>();
        failedResponses.put("count", getFailures());

        //failed
        if(getFailures()>0) {
            failedResponses.put("min",getKoMinTime());
            failedResponses.put("max",getKoMaxTime());
            failedResponses.put("avg",getKoMean());
            Map<String, Double>  percentile = new HashMap<>();
            for (String p : percentilesStringArray) {
                Float percentileValue = Float.valueOf(p.trim());
                percentile.put( p,getKoPercentile(percentileValue));
            }
            failedResponses.put("pct",percentile);
        }
        map.put("failedResponses",failedResponses);



        map.put("hitsCount",getHits());
        map.put("receivedBytes",getReceivedBytes());
        map.put("sentBytes",getSentBytes());
        map.put("isLast",isLastSend);
        map.put("sampleName", contextName);
        map.put("createdAt", timestampInSeconds);


        map.put("minActiveThreads", userMetric.getMinActiveThreads());
        map.put("maxActiveThreads", userMetric.getMaxActiveThreads());
        map.put("meanActiveThreads", userMetric.getMeanActiveThreads());
        map.put("finishedThreads", userMetric.getFinishedThreads());
        map.put("startedThreads", userMetric.getStartedThreads());
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }
    @Override
    public void add(SampleResult s){
        super.add(s);
        if(s.getStartTime() < startTime || startTime == -1){
            startTime = s.getStartTime();
        }
        if(s.getEndTime() > endTime || endTime == -1){
            endTime = s.getEndTime();
        }
    }
}
