package com.kc.bluetooth_ble;

import android.util.Log;

import static java.lang.Boolean.FALSE;

/**
 * @author qiuyongheng
 * @time 2017/3/17  11:10
 * @desc 心率算法
 */

public class Pulse_Sensor {
    private int sampleCounter = 0; //当前时间
    private int lastBeatTime = 0; //上一个心率脉冲时间
    private int IBI = 600; // 保存心率脉冲的周期
    private int THRESH = 25;
    private int thresh = THRESH; // 基准线
    private int T = THRESH; // 波谷
    private int Px = THRESH; // 波峰
    private boolean pulse = false; // true: 上升沿      false: 下降沿
    private boolean firstBeat = true;
    private boolean secondBeat = true;
    private int MAX_RATE_ID = 6;
    private int[] rate = new int[MAX_RATE_ID]; // 保存脉冲周期
    private int runningTotal;
    private int BPM = 0;
    private int amp = 100; // 振幅
    private boolean QS = false;

    /**
     * 每100ms采集一次数据, 返回心率
     */
    public int pulseSenosrCal(int signal) {
        if (signal < 0) {
            return 0;
        }
        sampleCounter += 100;
        int N = sampleCounter - lastBeatTime;

        /** 1. 寻找波峰波谷*/
        if (signal < thresh && N > (IBI / 5) * 3) {
            if (signal < T) {
                // 记录波谷
                T = signal;
            }
        }
        if (signal > thresh && signal > Px) {
            // 记录波峰
            Px = signal;
        }

        /** 2. */
        if (N > 250) { // 过滤高频噪音 心率大于250
            // 当ADC值大于基准线, 并且上一个脉冲是下降沿, 这个脉冲是上升沿
            if ((signal > thresh) && !pulse && (N > (IBI / 5) * 3)) { // 寻找波峰
                // 1. 下降沿
                pulse = true;
                // 2. 保存脉冲周期
                IBI = sampleCounter - lastBeatTime;
                lastBeatTime = sampleCounter;
                // 3. 过滤前2个心率脉冲
                if (firstBeat) {
                    firstBeat = false;
                    Log.e("====", "第一个心率过滤");
                    return 0;
                }

                if (secondBeat) {
                    secondBeat = false;
                    for (int i = 0; i <= 9; i++) {
                        // 记录脉冲的时间间隔, 初始化10个点(都是一样的值)
                        rate[i] = IBI;
                    }
                }

                // 心率周期
                runningTotal = 0;
                for (int i = 0; i <= MAX_RATE_ID - 2; i++) {
                    rate[i] = rate[i + 1];
                    runningTotal += rate[i];
                }

                // 保存当前周期
                rate[MAX_RATE_ID - 1] = IBI;
                runningTotal += rate[MAX_RATE_ID - 1];
                Log.e("====", "runningTotal = " + runningTotal);
                runningTotal /= MAX_RATE_ID;
                // 计算心率
                BPM = 60000 / runningTotal;
                QS = true;
                Log.e("====", "BPM = " + BPM);

                // 过滤不正常的心跳
                if (BPM > 55 && BPM < 150) {
                } else {
                    BPM = 0;
                }
            }
        }

        // 波谷
        if ((signal < thresh) && pulse) {
            pulse = false;
            // 计算振幅
            amp = Px - T;
            // 更新基准线
            thresh = amp / 2 + T;
            Log.e("========", "当前基准线: " + thresh);
            Px = thresh;
            T = thresh;
        }

        // 当2.5秒内没有心率脉冲时, 初始化
        if (N > 2500) {                             // if 2.5 seconds go by without a beat
            Log.e("========", "2.5秒内没有心率脉冲时, 初始化");
            thresh = THRESH;                          // set thresh default
            Px = THRESH;                               // set P default
            T = THRESH;                               // set T default

            lastBeatTime = sampleCounter;          // bring the lastBeatTime up to date
            firstBeat = true;                      // set these to avoid noise
            secondBeat = true;                     // when we get the heartbeat back
        }
        return getCurrentHeartRate();
    }

    private int getCurrentHeartRate() {
        if (QS) {
            QS = FALSE;
            return BPM;
        }
        return 0;
    }
}
