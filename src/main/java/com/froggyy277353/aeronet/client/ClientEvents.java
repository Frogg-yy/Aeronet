package com.froggyy277353.aeronet.client;

import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.AeroNet;
import com.froggyy277353.aeronet.client.keybind.ModKeyBindings;
import com.froggyy277353.aeronet.client.gui.NetworkResultReceiver;
import com.froggyy277353.aeronet.network.servernodespayloads.NetworkResultPayload;
import com.froggyy277353.aeronet.network.servernodespayloads.LinkSyncPayload;
import com.froggyy277353.aeronet.network.ServerLinks;
import com.froggyy277353.aeronet.client.gui.ServerSreen;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.physics.CompassNode;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.Command;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@EventBusSubscriber(value = Dist.CLIENT, modid = AeroNet.MOD_ID)
public class ClientEvents {
    private static final Set<ServerBlockEntity> COMPASS_SERVERS =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean compassDebugEnabled = true;

    public static void trackCompassServer(ServerBlockEntity server) {
        COMPASS_SERVERS.add(server);
    }

    public static void receiveNetworkResult(NetworkResultPayload payload) {
        if (Minecraft.getInstance().screen instanceof NetworkResultReceiver receiver) {
            receiver.receiveNetworkResult(payload.pos(), payload.result());
        }
    }
    @SubscribeEvent
    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(SurfaceScannerRenderer.ScannerRenderTypes.SCAN_PYRAMID);
    }

    public static void receiveLinkSync(LinkSyncPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var update = payload.node();
        if (mc.level.getBlockEntity(update.pos()) instanceof ServerBlockEntity be) {
            ServerLinks.applyClient(be.getNodes(), be.getConnections(), update.nodeData());
        }
        if (mc.screen instanceof ServerSreen screen && screen.getPos().equals(update.pos())) {
            screen.receiveLinkSync(update.nodeData());
        }
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {

    }
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.DELETE_NODE);
        event.register(ModKeyBindings.FRAME_NODE);

    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) {
            COMPASS_SERVERS.clear();
        }
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("aeronet")
                        .then(Commands.literal("compass_debug")
                                .executes(context -> {
                                    compassDebugEnabled = !compassDebugEnabled;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Compass debug: " + (compassDebugEnabled ? "ON" : "OFF")
                                            ),
                                            false
                                    );
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }

    @SubscribeEvent
    public static void renderCompassDebug(RenderLevelStageEvent event) {
        if (!compassDebugEnabled || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        COMPASS_SERVERS.removeIf(server ->
                server.isRemoved()
                        || server.getLevel() == null
                        || !server.getLevel().isClientSide
        );

        if (COMPASS_SERVERS.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        List<CompassDebugEntry> entries = new ArrayList<>();

        for (ServerBlockEntity server : COMPASS_SERVERS) {
            Vec3 start = Sable.HELPER.projectOutOfSubLevel(
                    server.getLevel(),
                    Vec3.atCenterOf(server.getBlockPos())
            );

            for (Node node : server.getNodes()) {
                if (!(node instanceof CompassNode compass)) {
                    continue;
                }

                if (compass.getInputArray().size() < 3
                        || compass.getOutputArray().size() < 4
                        || compass.getOutputArray().get(3).isEmpty()
                        || compass.getInputArray().stream().anyMatch(NodeValue::isEmpty)) {
                    continue;
                }

                Vec3 target = new Vec3(
                        compass.getInputArray().get(0).asFloat(),
                        compass.getInputArray().get(1).asFloat(),
                        compass.getInputArray().get(2).asFloat()
                );

                drawCompassLine(poseStack, lines, camera, start, target);
                drawTargetMarker(poseStack, lines, camera, target);
                entries.add(new CompassDebugEntry(target, compass));
            }
        }

        buffers.endBatch(RenderType.lines());
        for (CompassDebugEntry entry : entries) {
            drawCompassAngles(poseStack, buffers, camera, entry.target(), entry.compass());
        }
        buffers.endBatch();
    }

    private static void drawCompassLine(
            PoseStack poseStack,
            VertexConsumer lines,
            Vec3 camera,
            Vec3 start,
            Vec3 target
    ) {
        Vec3 direction = target.subtract(start);
        if (direction.lengthSqr() < 1.0E-8) {
            return;
        }

        Vec3 normal = direction.normalize();
        Vec3 renderStart = start.subtract(camera);
        Vec3 renderTarget = target.subtract(camera);
        PoseStack.Pose pose = poseStack.last();

        lines.addVertex(pose.pose(), (float) renderStart.x, (float) renderStart.y, (float) renderStart.z)
                .setColor(0.1F, 1.0F, 0.3F, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        lines.addVertex(pose.pose(), (float) renderTarget.x, (float) renderTarget.y, (float) renderTarget.z)
                .setColor(0.1F, 1.0F, 0.3F, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    private static void drawTargetMarker(
            PoseStack poseStack,
            VertexConsumer lines,
            Vec3 camera,
            Vec3 target
    ) {
        AABB marker = new AABB(
                target.x - 0.2,
                target.y - 0.2,
                target.z - 0.2,
                target.x + 0.2,
                target.y + 0.2,
                target.z + 0.2
        ).move(-camera.x, -camera.y, -camera.z);

        LevelRenderer.renderLineBox(
                poseStack,
                lines,
                marker,
                1.0F,
                0.2F,
                0.2F,
                1.0F
        );
    }

    private static void drawCompassAngles(
            PoseStack poseStack,
            MultiBufferSource.BufferSource buffers,
            Vec3 camera,
            Vec3 target,
            CompassNode compass
    ) {
        String text = String.format(
                Locale.ROOT,
                "X %.3f  Y %.3f  Z %.3f  D %.3f",
                compass.getOutputArray().get(0).asFloat(),
                compass.getOutputArray().get(1).asFloat(),
                compass.getOutputArray().get(2).asFloat(),
                compass.getOutputArray().get(3).asFloat()
        );

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Vec3 renderTarget = target.subtract(camera);

        poseStack.pushPose();
        poseStack.translate(renderTarget.x, renderTarget.y + 0.5, renderTarget.z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);

        float textX = -font.width(text) / 2.0F;
        font.drawInBatch(
                text,
                textX,
                0.0F,
                0xFFFFFFFF,
                false,
                poseStack.last().pose(),
                buffers,
                Font.DisplayMode.SEE_THROUGH,
                0x55000000,
                0xF000F0
        );
        poseStack.popPose();
    }

    private record CompassDebugEntry(Vec3 target, CompassNode compass) {
    }

    public static final ModelResourceLocation SCANNER_SENSOR_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath("aeronet", "block/surface_scanner/sensor")
    );
    public static final ModelResourceLocation BUTTON_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath("aeronet", "block/button/button_bb")
    );
    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        event.register(SCANNER_SENSOR_MODEL);
        event.register(BUTTON_MODEL);
    }
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.SCANNER_BLOCK_ENTITY.get(),
                SurfaceScannerRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.BUTTON_BLOCK_ENTITY.get(),
                ButtonRenderer::new);
    }

}
