package com.xxmicloxx.NoteBlockAPI.utils;

import net.minecraft.world.phys.Vec3;

public class MathUtils {

	private static double[] cos = new double[360];
	private static double[] sin = new double[360];

	static {
		for (int deg = 0; deg < 360; deg++) {
			cos[deg] = Math.cos(Math.toRadians(deg));
			sin[deg] = Math.sin(Math.toRadians(deg));
		}
	}

	private static double[] getCos(){
		return cos;
	}

	private static double[] getSin(){
		return sin;
	}

	public static Vec3 stereoSourceLeft(Vec3 pos, float yRot, float distance) {
		int angle = getAngle(yRot);
		Vec3 cloned = new Vec3(pos.x, pos.y, pos.z);
	    return cloned.add(-getCos()[angle] * distance, 0, -getSin()[angle] * distance);
	}
	public static Vec3 stereoSourceRight(Vec3 pos, float yRot, float distance) {
		int angle = getAngle(yRot);
		Vec3 cloned = new Vec3(pos.x, pos.y, pos.z);
	    return cloned.add(getCos()[angle] * distance, 0, getSin()[angle] * distance);
	}

	/**
	 * Calculate new Vec3 for stereo
	 * @param pos origin pos
	 * @param distance negative for left side, positive for right side
	 * @return
	 */
	public static Vec3 stereoPan(Vec3 pos, float yRot, float distance){
		int angle = getAngle(yRot);
		Vec3 cloned = new Vec3(pos.x, pos.y, pos.z);
		return cloned.add( getCos()[angle] * distance, 0, getSin()[angle] * distance);
	}

	private static int getAngle(float yaw){
		int angle = (int) yaw;
		while (angle < 0) angle += 360;
		return angle % 360;
	}

}
