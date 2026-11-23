package com.froggyy277353.aeronet.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.aeronet";

    public static final KeyMapping FRAME_NODE = new KeyMapping(
            "key.aeronet.frame_node", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PERIOD, CATEGORY);

    public static final KeyMapping DELETE_NODE =
            new KeyMapping(
                    "key.aeronet.delete_node",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    CATEGORY
            );

}
