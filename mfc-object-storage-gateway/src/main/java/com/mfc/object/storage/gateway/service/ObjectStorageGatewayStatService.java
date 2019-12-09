package com.mfc.object.storage.gateway.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ObjectStorageGatewayStatService {

    private final Logger LOG = LoggerFactory.getLogger(ObjectStorageGatewayStatService.class);

    private Map<String, StatModel> statisticsMap = new ConcurrentHashMap<String, StatModel>();

    @PostConstruct
    void init() throws Exception {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    print();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void put(String api, long time) {
        StatModel stati = statisticsMap.get(api);
        if (stati == null) {
            statisticsMap.put(api, new StatModel(api, time, time, time, 1, time));
        } else {
            stati.setCount(stati.getCount() + 1);
            if (stati.getMinTime() > time)
                stati.setMinTime(time);
            if (stati.getMaxTime() < time)
                stati.setMaxTime(time);
            stati.setTotalTime(stati.getTotalTime() + time);
            stati.setAvgTime(stati.getTotalTime() / stati.getCount());
            statisticsMap.put(api, stati);
        }
    }

    private void print() {
        statisticsMap.values().forEach(statistic -> {
            StatModel stati = statisticsMap.remove(statistic.getApi());
            if (stati == null)
                return;
            LOG.info("--------------------------------------------------------------------");
            LOG.info("API stat: {} : Count {},minTime {},maxTime {},avgTime{}", stati.getApi(), stati.getCount(),
                    stati.getMinTime(), stati.getMaxTime(), stati.getAvgTime());
            LOG.info("-------------------------------------------------------------------");

        });

    }

    class StatModel {

        private String api;
        private long minTime;
        private long maxTime;
        private long totalTime;
        private long avgTime;
        private long count;

        public StatModel(String api, long minTime, long maxTime, long avgTime, long count, long totalTime) {
            super();
            this.api = api;
            this.minTime = minTime;
            this.maxTime = maxTime;
            this.avgTime = avgTime;
            this.totalTime = totalTime;
            this.count = count;
        }

        public String getApi() {
            return api;
        }

        public void setApi(String api) {
            this.api = api;
        }

        public long getMinTime() {
            return minTime;
        }

        public void setMinTime(long minTime) {
            this.minTime = minTime;
        }

        public long getMaxTime() {
            return maxTime;
        }

        public void setMaxTime(long maxTime) {
            this.maxTime = maxTime;
        }

        public long getAvgTime() {
            return avgTime;
        }

        public void setAvgTime(long avgTime) {
            this.avgTime = avgTime;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(long totalTime) {
            this.totalTime = totalTime;
        }

    }

}
