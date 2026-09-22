package org.tomdang.islandedge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class OceanTransitionProfile {
    static final int BLEND_DISTANCE = 192;
    private final Map<Long, List<Sample>> buckets;

    private OceanTransitionProfile(Map<Long, List<Sample>> buckets) {
        this.buckets = buckets;
    }

    static OceanTransitionProfile load(InputStream stream) throws IOException {
        Map<Long, List<Sample>> buckets = new HashMap<>();
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split(",", -1);
                if (fields.length != 4) throw new IOException("Invalid ocean seam line " + lineNumber);
                try {
                    Sample sample = new Sample(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]),
                            Integer.parseInt(fields[2]), fields[3]);
                    buckets.computeIfAbsent(key(sample.x() >> 4, sample.z() >> 4), ignored -> new ArrayList<>())
                            .add(sample);
                } catch (NumberFormatException exception) {
                    throw new IOException("Invalid ocean seam number on line " + lineNumber, exception);
                }
            }
        }
        if (buckets.isEmpty()) throw new IOException("Ocean seam profile is empty");
        return new OceanTransitionProfile(Map.copyOf(buckets));
    }

    Match nearest(int x, int z) {
        int radius = (BLEND_DISTANCE + 15) >> 4;
        int centerX = x >> 4;
        int centerZ = z >> 4;
        Sample nearest = null;
        int nearestSquared = BLEND_DISTANCE * BLEND_DISTANCE + 1;
        for (int cz = centerZ - radius; cz <= centerZ + radius; cz++) {
            for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
                List<Sample> samples = buckets.get(key(cx, cz));
                if (samples == null) continue;
                for (Sample sample : samples) {
                    int dx = x - sample.x();
                    int dz = z - sample.z();
                    int squared = dx * dx + dz * dz;
                    if (squared < nearestSquared) {
                        nearestSquared = squared;
                        nearest = sample;
                    }
                }
            }
        }
        return nearest == null ? null : new Match(nearest, Math.sqrt(nearestSquared));
    }

    private static long key(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    record Sample(int x, int z, int floorY, String material) {}
    record Match(Sample sample, double distance) {}
}
