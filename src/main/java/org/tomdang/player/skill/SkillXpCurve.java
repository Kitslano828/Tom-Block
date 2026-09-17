package org.tomdang.player.skill;

/** Cumulative skill XP thresholds. Level 0 starts at zero; level 100 is the cap. */
public final class SkillXpCurve {
	public static final int MAX_LEVEL = 100;
	private static final int[] REFERENCE_STEPS = {
		50,125,200,300,500,750,1000,1500,2000,3500,
		5000,7500,10000,15000,20000,30000,50000,75000,100000,200000,
		300000,400000,500000,600000,700000,800000,900000,1000000,1100000,1200000,
		1300000,1400000,1500000,1600000,1700000,1800000,1900000,2000000,2100000,2200000,
		2300000,2400000,2500000,2600000,2750000,2900000,3100000,3400000,3700000,4000000,
		4300000,4600000,4900000,5200000,5500000,5800000,6100000,6400000,6700000,7000000
	};
	private static final long[] CUMULATIVE = buildThresholds();

	private SkillXpCurve() { }

	public static long totalXpForLevel(int level) {
		if (level < 0 || level > MAX_LEVEL) throw new IllegalArgumentException("level must be 0..100");
		return CUMULATIVE[level];
	}

	public static long xpForLevel(int level) {
		if (level < 1 || level > MAX_LEVEL) throw new IllegalArgumentException("level must be 1..100");
		return CUMULATIVE[level] - CUMULATIVE[level - 1];
	}

	public static int levelForXp(long totalXp) {
		if (totalXp < 0) throw new IllegalArgumentException("total XP cannot be negative");
		int low = 0, high = MAX_LEVEL;
		while (low < high) {
			int middle = (low + high + 1) >>> 1;
			if (CUMULATIVE[middle] <= totalXp) low = middle;
			else high = middle - 1;
		}
		return low;
	}

	private static long[] buildThresholds() {
		long[] thresholds = new long[MAX_LEVEL + 1];
		for (int level = 1; level <= MAX_LEVEL; level++) {
			// The supplied table ends at 60. Continue its 300,000 XP/level late-game slope.
			long referenceStep = level <= REFERENCE_STEPS.length
					? REFERENCE_STEPS[level - 1]
					: 7_000_000L + 300_000L * (level - 60);
			long scaledStep = Math.round(referenceStep * 0.7);
			thresholds[level] = Math.addExact(thresholds[level - 1], scaledStep);
		}
		return thresholds;
	}
}
