package dev.mcannavan.dotdash;

public interface IMorseTiming {
    float getDitLength();
    float getDahLength();
    float getInterCharLength();
    float getIntraCharLength();
    float getInterWordLength();
    float getWpm();

    //public void calculateSpeedFromMillis(float ms);
    //public void calculateSpeedFromWpm(float wpm);

}
