package com.froggyy277353.aeronet.block.entity;

/** Angles are accumulated degrees; 360 is one complete turn, not zero. */
public final class ServoMotion {
    private ServoMotion() {}

    public static float modifier(double current, double target, float inputSpeed) {
        if (!Double.isFinite(current) || !Double.isFinite(target)
                || !Float.isFinite(inputSpeed) || inputSpeed == 0) return 0;
        double remaining = target - current;
        if (Math.abs(remaining) < 1.0E-5) return 0;
        // Create uses RPM: one tick at 1 RPM rotates the shaft by 0.3 degrees.
        return (float) Math.clamp(remaining / (inputSpeed * 0.3), -1.0, 1.0);
    }
}
