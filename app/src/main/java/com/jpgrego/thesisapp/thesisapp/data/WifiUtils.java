package com.jpgrego.thesisapp.thesisapp.data;

/**
 * Created by jgrego on 05-09-2016.
 */
public class WifiUtils {
    /*
     * Thought out by myself... please excuse me for any mistake. I wanted an alternative to
     * creating an array or an HashMap, though not sure if this is better in terms of performance!
     * (jpgrego)
     */
     static int frequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2472) {                             // 802.11 b/g/n (2.4 GHz)
            return frequency % 2412 / 5 + 1;
        } else if (frequency == 2484) {
            return 14;
        } else if (frequency >= 4915 && frequency <= 4980) {                      // 802.11 a/h/j/n/ac (5 GHz)
            return frequency % 4915 / 5 + 183;
        } else if (frequency >= 5035 && frequency <= 5080) {
            return frequency % 5035 / 5 + 7;
        } else if (frequency >= 5170 && frequency <= 5320) {
            return frequency % 5170 / 5 + 34;
        } else if (frequency >= 5500 && frequency <= 5825){
            return frequency % 5500 / 5 + 100;
        } else {
            return -1;
        }
    }

}
