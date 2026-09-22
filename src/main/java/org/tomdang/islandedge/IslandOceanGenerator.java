package org.tomdang.islandedge;

import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

/** Generates an endless, quiet ocean beyond the imported island chunks. */
public final class IslandOceanGenerator extends ChunkGenerator {
    private static final int SEA_SURFACE = 62;
    private static final int STONE_BASE_TOP = 40;
    private static final BiomeProvider OCEAN_BIOMES = new BiomeProvider() {
        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return Biome.OCEAN;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(Biome.OCEAN);
        }
    };
    private final OceanTransitionProfile transitionProfile;

    IslandOceanGenerator(OceanTransitionProfile transitionProfile) {
        this.transitionProfile = transitionProfile;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return OCEAN_BIOMES;
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ,
                              ChunkData data) {
        int minY = data.getMinHeight();
        int maxY = data.getMaxHeight();
        if (minY < STONE_BASE_TOP) {
            data.setRegion(0, minY, 0, 16, STONE_BASE_TOP, 16, Material.STONE);
        }
        if (maxY <= STONE_BASE_TOP) return;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = chunkX * 16 + localX;
                int z = chunkZ * 16 + localZ;
                int floor = seabedY(x, z, transitionProfile);
                if (floor > STONE_BASE_TOP) {
                    data.setRegion(localX, STONE_BASE_TOP, localZ,
                            localX + 1, Math.min(floor, maxY), localZ + 1, Material.STONE);
                }
                if (floor >= minY && floor < maxY) {
                    data.setBlock(localX, floor, localZ, seabedMaterial(x, z, transitionProfile));
                }
                if (floor + 1 < maxY) {
                    data.setRegion(localX, Math.max(floor + 1, minY), localZ,
                            localX + 1, Math.min(SEA_SURFACE + 1, maxY), localZ + 1,
                            Material.WATER);
                }
            }
        }
    }

    static int seabedY(int x, int z, OceanTransitionProfile profile) {
        int naturalFloor = naturalSeabedY(x, z);
        OceanTransitionProfile.Match match = profile.nearest(x, z);
        if (match == null) return naturalFloor;
        double amount = smoothStep(match.distance() / OceanTransitionProfile.BLEND_DISTANCE);
        return (int) Math.round(match.sample().floorY()
                + (naturalFloor - match.sample().floorY()) * amount);
    }

    static int naturalSeabedY(int x, int z) {
        double broad = valueNoise(x, z, 96, 0x4f1bbcdcL);
        double detail = valueNoise(x, z, 37, 0x7a2d938fL);
        return 49 + (int) Math.round(broad * 3.0 + detail * 1.5);
    }

    private static Material seabedMaterial(int x, int z, OceanTransitionProfile profile) {
        OceanTransitionProfile.Match match = profile.nearest(x, z);
        if (match != null && match.distance() < 24) {
            Material imported = Material.matchMaterial(match.sample().material());
            if (imported != null && imported.isBlock() && imported.isSolid()) return imported;
        }
        return hash(x, z, 0x61c88647L) % 5 == 0 ? Material.SAND : Material.GRAVEL;
    }

    private static double smoothStep(double value) {
        double clamped = Math.max(0.0, Math.min(1.0, value));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    private static double valueNoise(int x, int z, int scale, long seed) {
        int x0 = Math.floorDiv(x, scale);
        int z0 = Math.floorDiv(z, scale);
        double fx = Math.floorMod(x, scale) / (double) scale;
        double fz = Math.floorMod(z, scale) / (double) scale;
        fx = smoothStep(fx);
        fz = smoothStep(fz);
        double a = unitHash(x0, z0, seed);
        double b = unitHash(x0 + 1, z0, seed);
        double c = unitHash(x0, z0 + 1, seed);
        double d = unitHash(x0 + 1, z0 + 1, seed);
        return lerp(lerp(a, b, fx), lerp(c, d, fx), fz);
    }

    private static double unitHash(int x, int z, long seed) {
        return (hash(x, z, seed) / (double) Long.MAX_VALUE) * 2.0 - 1.0;
    }

    private static long hash(int x, int z, long seed) {
        long value = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return (value ^ (value >>> 31)) & Long.MAX_VALUE;
    }

    private static double lerp(double from, double to, double amount) {
        return from + (to - from) * amount;
    }

    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateMobs() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }
}
