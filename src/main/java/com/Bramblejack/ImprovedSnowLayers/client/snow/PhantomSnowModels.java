package com.Bramblejack.ImprovedSnowLayers.client.snow;

import com.Bramblejack.ImprovedSnowLayers.ImprovedSnowLayersMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.StringReader;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ImprovedSnowLayersMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PhantomSnowModels {

    private static final BakedModel[] WIDENED_MODELS = new BakedModel[8];

    private static ModelBaker dummyBaker(Function<Material, TextureAtlasSprite> spriteGetter) {
        return new ModelBaker() {
            @Override
            public UnbakedModel getModel(ResourceLocation location) {
                throw new UnsupportedOperationException(
                        "Phantom snow models have no parent to resolve (requested " + location + ")");
            }

            @Override
            public BakedModel bake(ResourceLocation location, ModelState state) {
                throw new UnsupportedOperationException(
                        "Phantom snow models have no sub-models to bake (requested " + location + ")");
            }

            @Override
            public BakedModel bake(ResourceLocation location, ModelState state, Function<Material, TextureAtlasSprite> textureGetter) {
                throw new UnsupportedOperationException(
                        "Phantom snow models have no sub-models to bake (requested " + location + ")");
            }

            @Override
            public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
                return spriteGetter;
            }
        };
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        Function<Material, TextureAtlasSprite> spriteGetter = material ->
                Minecraft.getInstance().getModelManager().getAtlas(material.atlasLocation()).getSprite(material.texture());
        ModelBaker baker = dummyBaker(spriteGetter);

        for (int layers = 1; layers <= 7; layers++) {
            ResourceLocation id = new ResourceLocation(ImprovedSnowLayersMod.MODID, "block/phantom_snow_layer_" + layers);
            BlockModel unbaked = BlockModel.fromStream(new StringReader(widenedModelJson(layers)));
            WIDENED_MODELS[layers] = unbaked.bake(baker, spriteGetter, BlockModelRotation.X0_Y0, id);
        }
    }

    private static String widenedModelJson(int layers) {
        int height = layers * 2;
        int uvV0 = 16 - height;
        return """
                {
                  "textures": { "particle": "minecraft:block/snow", "texture": "minecraft:block/snow" },
                  "elements": [ {
                    "from": [ -0.005, 0, -0.005 ],
                    "to": [ 16.005, %1$d, 16.005 ],
                    "faces": {
                      "down":  { "uv": [ 0, 0, 16, 16 ], "texture": "#texture", "cullface": "down" },
                      "up":    { "uv": [ 0, 0, 16, 16 ], "texture": "#texture" },
                      "north": { "uv": [ 0, %2$d, 16, 16 ], "texture": "#texture", "cullface": "north" },
                      "south": { "uv": [ 0, %2$d, 16, 16 ], "texture": "#texture", "cullface": "south" },
                      "west":  { "uv": [ 0, %2$d, 16, 16 ], "texture": "#texture", "cullface": "west" },
                      "east":  { "uv": [ 0, %2$d, 16, 16 ], "texture": "#texture", "cullface": "east" }
                    }
                  } ]
                }
                """.formatted(height, uvV0);
    }

    public static BakedModel getWidenedModel(int layers) {
        if (layers < 1 || layers > 7) return null;
        return WIDENED_MODELS[layers];
    }
}
