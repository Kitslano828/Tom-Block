package org.tomdang.islandedge;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class IslandOceanGeneratorTest {
    @Test
    void seabedRemainsBelowTheImportedSeaAndChangesSmoothly() {
        for (int x = -1500; x <= -400; x += 17) {
            for (int z = -200; z <= 1000; z += 19) {
                int floor = IslandOceanGenerator.naturalSeabedY(x, z);
                assertTrue(floor >= 44 && floor <= 54);
                assertTrue(Math.abs(floor - IslandOceanGenerator.naturalSeabedY(x + 1, z)) <= 1);
                assertTrue(Math.abs(floor - IslandOceanGenerator.naturalSeabedY(x, z + 1)) <= 1);
            }
        }
    }

    @Test
    void importedFloorBlendsIntoNaturalOcean() throws Exception {
        var profile = OceanTransitionProfile.load(new ByteArrayInputStream(
                "0,0,30,stone\n".getBytes(StandardCharsets.UTF_8)));
        assertTrue(Math.abs(IslandOceanGenerator.seabedY(0, 0, profile) - 30) <= 1);
        int farFloor = IslandOceanGenerator.seabedY(OceanTransitionProfile.BLEND_DISTANCE, 0, profile);
        assertTrue(Math.abs(farFloor - IslandOceanGenerator.naturalSeabedY(
                OceanTransitionProfile.BLEND_DISTANCE, 0)) <= 1);
    }

    @Test
    void packagedSouthwestProfileLoadsAndMatchesItsFirstSample() throws Exception {
        Path resource = Path.of("src/island-edge/resources/ocean-seam.csv");
        var profile = OceanTransitionProfile.load(Files.newInputStream(resource));
        String first = Files.lines(resource, StandardCharsets.UTF_8)
                .findFirst().orElseThrow();
        String[] fields = first.split(",");
        int x = Integer.parseInt(fields[0]);
        int z = Integer.parseInt(fields[1]);
        int importedFloor = Integer.parseInt(fields[2]);
        assertTrue(Math.abs(IslandOceanGenerator.seabedY(x, z, profile) - importedFloor) <= 1);
    }
}
