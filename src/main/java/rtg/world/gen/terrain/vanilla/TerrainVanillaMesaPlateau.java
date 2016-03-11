package rtg.world.gen.terrain.vanilla;

import rtg.util.CellNoise;
import rtg.util.OpenSimplexNoise;
import rtg.world.gen.terrain.TerrainBase;

public class TerrainVanillaMesaPlateau extends TerrainBase
{
	private float[] height;
	private int heightLength;
	private float smooth;
	private float base;
	
	/*
	 * Example parameters:
	 * 
	 * allowed to generate rivers?
	 * riverGen = true
	 * 
	 * canyon jump heights
	 * heightArray = new float[]{2.0f, 0.5f, 6.5f, 0.5f, 14.0f, 0.5f, 19.0f, 0.5f}
	 * 
	 * strength of canyon jump heights
	 * heightStrength = 35f
	 * 
	 * canyon width (cliff to cliff)
	 * canyonWidth = 160f
	 * 
	 * canyon heigth (total heigth)
	 * canyonHeight = 60f
	 * 
	 * canyon strength
	 * canyonStrength = 40f
	 * 
	 */
	public TerrainVanillaMesaPlateau(boolean riverGen, float heightStrength, float canyonWidth, float canyonHeight, float canyonStrength, float baseHeight)
	{
		/**    Values come in pairs per layer. First is how high to step up.
		 * 	Second is a value between 0 and 1, signifying when to step up.
		 */
		height = new float[]{32.0f, 0.4f, 20f, 0.7f};
		/**
		 * lower values = smoother.
		 */
		smooth = 50f;
		heightLength = height.length;
		base = 69f;
	}

	@Override
	public float generateNoise(OpenSimplexNoise simplex, CellNoise cell, int x, int y, float border, float river)
	{
		river *= 0.5f;
		river = river > 1f ? 1f : river;
		float b = simplex.noise2(x / 40f, y / 40f) * 1.5f;
		b *= river;

		float sn = simplex.noise2(x / 100f, y / 100f) * 0.5f + 0.5f;
		sn += simplex.noise2(x / 8f, y / 8f) * 0.02 + 0.02f;
		float n;
		for (int i = 0; i < heightLength; i += 2) {
			n = (sn - height[i + 1]) / (1 - height[i + 1]);
			n = n * smooth;
			n = (n < 0) ? 0 : (n > 1) ? 1 : n;
			if (sn > height[i + 1]) {
				b += (height[i] * n);
			}
		}

		return base + b;
	}
}