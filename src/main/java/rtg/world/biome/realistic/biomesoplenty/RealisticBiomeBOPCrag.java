package rtg.world.biome.realistic.biomesoplenty;

import java.util.Random;

import biomesoplenty.api.biome.BOPBiomes;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import rtg.api.config.BiomeConfig;
import rtg.api.util.WorldUtil.Terrain;
import rtg.api.util.noise.SimplexNoise;
import rtg.api.world.RTGWorld;
import rtg.api.world.deco.DecoBaseBiomeDecorations;
import rtg.api.world.deco.DecoPond;
import rtg.api.world.deco.helper.DecoHelperBorder;
import rtg.api.world.surface.SurfaceBase;
import rtg.api.world.terrain.TerrainBase;


public class RealisticBiomeBOPCrag extends RealisticBiomeBOPBase {

    public static Biome biome = BOPBiomes.crag.orNull();
    public static Biome river = Biomes.RIVER;

    // removed
    //private static IBlockState cragRock = BOPBlocks.crag_rock.getDefaultState();

    public RealisticBiomeBOPCrag() {

        super(biome);

/* 1.12 property removed
        // Prevent dirt from messing up the surface.
        this.rDecorator().dirtSize = 0;
*/
    }

    @Override
    public void initConfig() {
        this.getConfig().ALLOW_RIVERS.set(false);
        this.getConfig().ALLOW_SCENIC_LAKES.set(false);
        this.getConfig().addProperty(this.getConfig().SURFACE_MIX_BLOCK).set("");
    }

    @Override
    public TerrainBase initTerrain() {
        return new TerrainBOPCrag(false, new float[]{2.0f, 0.5f, 6.5f, 0.5f, 14.0f, 0.5f, 19.0f, 0.5f, 23.0f, 0.5f}, 35f, 80f, 60f, 40f, 69f);
    }

    @Override
    public SurfaceBase initSurface() {

        return new SurfaceBOPCrag(getConfig(), Blocks.STONE.getDefaultState(), Blocks.DIRT.getDefaultState(), 0f, 1.5f, 60f, 65f, 1.5f, Blocks.GRASS.getDefaultState(), 0f);
    }

    @Override
    public void initDecos() {

        DecoPond decoPond = new DecoPond();
        decoPond.setChunksPerPond(8);
        DecoHelperBorder borderedPond = new DecoHelperBorder(decoPond, 0.8f, 0.7f);
        this.addDeco(borderedPond);

        this.addDeco(new DecoBaseBiomeDecorations());
    }

    @Override
    public boolean generatesEmeralds() {
        return true;
    }

    public class TerrainBOPCrag extends TerrainBase {

        private boolean booRiver;
        private float[] height;
        private int heightLength;
        private float strength;
        private float cWidth;
        private float cHeigth;
        private float cStrength;
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
        public TerrainBOPCrag(boolean riverGen, float[] heightArryay, float heightStrength, float canyonWidth, float canyonHeight, float canyonStrength, float baseHeight) {
            booRiver = riverGen;
            height = heightArryay;
            strength = heightStrength;
            heightLength = height.length;
            cWidth = canyonWidth;
            cHeigth = canyonHeight;
            cStrength = canyonStrength;
            base = baseHeight;
        }

        @Override
        public float generateNoise(RTGWorld rtgWorld, int x, int y, float border, float river) {

            SimplexNoise simplex = rtgWorld.simplexInstance(0);
            //float b = simplex.noise2(x / cWidth, y / cWidth) * cHeigth * river;
            //b *= b / cStrength;
            river *= 1.3f;
            river = river > 1f ? 1f : river;
            float r = simplex.noise2f(x / 100f, y / 100f) * 50f;
            r = r < -7.4f ? -7.4f : r > 7.4f ? 7.4f : r;
            float b = (17f + r) * river;

            float hn = simplex.noise2f(x / 12f, y / 12f) * 0.5f;
            float sb = 0f;
            if (b > 0f) {
                sb = b;
                sb = sb < 0f ? 0f : sb > 7f ? 7f : sb;
                sb = hn * sb;
            }
            b += sb;

            float cTotal = 0f;
            float cTemp = 0f;

            for (int i = 0; i < heightLength; i += 2) {
                cTemp = 0;
                if (b > height[i] && border > 0.6f + (height[i] * 0.015f) + hn * 0.2f) {
                    cTemp = b > height[i] + height[i + 1] ? height[i + 1] : b - height[i];
                    cTemp *= strength;
                }
                cTotal += cTemp;
            }


            float bn = 0f;
            if (booRiver) {
                if (b < 5f) {
                    bn = 5f - b;
                    for (int i = 0; i < 3; i++) {
                        bn *= bn / 4.5f;
                    }
                }
            }
            else if (b < 5f) {
                bn = (simplex.noise2f(x / 7f, y / 7f) * 1.3f + simplex.noise2f(x / 15f, y / 15f) * 2f) * (5f - b) * 0.2f;
            }

            b += cTotal - bn;

            return base + b;
        }
    }

    public class SurfaceBOPCrag extends SurfaceBase {

        private float min;

        private float sCliff = 1.5f;
        private float sHeight = 60f;
        private float sStrength = 65f;
        private float cCliff = 1.5f;

        private IBlockState mixBlock;
        private float mixHeight;

        public SurfaceBOPCrag(BiomeConfig config, IBlockState top, IBlockState fill, float minCliff, float stoneCliff,
                              float stoneHeight, float stoneStrength, float clayCliff, IBlockState mix, float mixSize) {

            super(config, top, fill);
            min = minCliff;

            sCliff = stoneCliff;
            sHeight = stoneHeight;
            sStrength = stoneStrength;
            cCliff = clayCliff;

            mixBlock = this.getConfigBlock(config.SURFACE_MIX_BLOCK.get(), mix);
            mixHeight = mixSize;
        }

        @Override
        public void paintTerrain(ChunkPrimer primer, int i, int j, int x, int z, int depth, RTGWorld rtgWorld, float[] noise, float river, Biome[] base) {

            Random rand = rtgWorld.rand();
            SimplexNoise simplex = rtgWorld.simplexInstance(0);
            float c = Terrain.calcCliff(x, z, noise);
            int cliff = 0;
            boolean m = false;

            Block b;
            for (int k = 255; k > -1; k--) {
                b = primer.getBlockState(x, k, z).getBlock();
                if (b == Blocks.AIR) {
                    depth = -1;
                }
                else if (b == Blocks.STONE) {
                    depth++;

                    if (depth == 0) {

                        float p = simplex.noise3f(i / 8f, j / 8f, k / 8f) * 0.5f;
                        if (c > min && c > sCliff - ((k - sHeight) / sStrength) + p) {
                            cliff = 1;
                        }
                        if (c > cCliff) {
                            cliff = 2;
                        }

                        if (cliff == 1) {
                            if (rand.nextInt(3) == 0) {

                                primer.setBlockState(x, k, z, hcCobble(rtgWorld, i, j, x, z, k));
                            }
                            else {

                                primer.setBlockState(x, k, z, topBlock);
                            }
                        }
                        else if (cliff == 2) {
                            primer.setBlockState(x, k, z, topBlock);
                        }
                        else if (k < 63) {
                            if (k < 62) {
                                primer.setBlockState(x, k, z, fillerBlock);
                            }
                            else {
                                primer.setBlockState(x, k, z, topBlock);
                            }
                        }
                        else if (simplex.noise2f(i / 12f, j / 12f) > mixHeight) {

                            if (rand.nextInt(3) != 0) {
                                primer.setBlockState(x, k, z, mixBlock);
                            }
                            else {
                                primer.setBlockState(x, k, z, topBlock);
                            }
                        }
                        else {
                            primer.setBlockState(x, k, z, topBlock);
                        }
                    }
                    else if (depth < 6) {
                        if (cliff == 1) {
                            primer.setBlockState(x, k, z, topBlock);
                        }
                        else if (cliff == 2) {
                            primer.setBlockState(x, k, z, topBlock);
                        }
                        else {
                            primer.setBlockState(x, k, z, fillerBlock);
                        }
                    }
                }
            }
        }

        @Override
        protected IBlockState hcCobble(RTGWorld rtgWorld, int worldX, int worldZ, int chunkX, int chunkZ, int worldY) {
            // return cragRock; removed
            return Blocks.STONE.getDefaultState();
        }
    }
}
