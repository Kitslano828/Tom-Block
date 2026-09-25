package org.tomdang.hud.text;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.assertEquals;
class MinecraftDefaultTextWidthServiceTest{
	private final MinecraftDefaultTextWidthService widths=new MinecraftDefaultTextWidthService();
	@Test void matchesBitmapAdvanceForNarrowGlyphs(){assertEquals(2,widths.measure("i"));assertEquals(3,widths.measure("l"));assertEquals(3,widths.measure("`"));}
	@Test void repeatedLowercaseLDoesNotAccumulateCursorError(){assertEquals(6,widths.measure("ll"));}
}
