package org.tomdang.mining.miningstats;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.tomdang.mining.MiningService;
import org.tomdang.mining.miningblock.MiningBlock;
import org.tomdang.player.PlayerProfile;

import java.util.concurrent.ThreadLocalRandom;

public class MiningFortune {

	public MiningFortune() {
	}

	public boolean checkForMiningFortuneProc(double totalFortune) {
		return rollForFortune(totalFortune % 100);
	}

	public int guaranteedDrops(double fortune) {
		if (fortune >= 100) {
			return (int)fortune / 100;
		}
		return 0;
	}

	public boolean rollForFortune(double chance) { // FOR LATER
		if (chance <= 0.0) return false;
		if (chance >= 100.0) return true;

		double roll = ThreadLocalRandom.current().nextDouble(100.0);
		return roll < chance;
	}
}
