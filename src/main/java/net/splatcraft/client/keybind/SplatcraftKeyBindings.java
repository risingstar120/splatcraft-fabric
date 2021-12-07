package net.splatcraft.client.keybind;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.splatcraft.Splatcraft;
import net.splatcraft.client.config.ClientConfig;
import net.splatcraft.client.config.SplatcraftConfigScreenFactory;
import net.splatcraft.client.network.NetworkingClient;
import net.splatcraft.component.PlayerDataComponent;
import net.splatcraft.util.SplatcraftUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SplatcraftKeyBindings {
    public static final String KEY_CATEGORY = "key.categories.%s".formatted(Splatcraft.MOD_ID);

    public static final ToggleKeyBinding CHANGE_SQUID_FORM = register("change_squid_form", GLFW.GLFW_KEY_Z, ToggleKeyBinding::new);
    public static final KeyBinding OPEN_CONFIG = register("open_config", GLFW.GLFW_KEY_UNKNOWN);

    static {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    // config menu
                    if (OPEN_CONFIG.wasPressed()) {
                        client.setScreen(new SplatcraftConfigScreenFactory().create(client.currentScreen));
                        return;
                    }

                    //---

                    PlayerDataComponent data = PlayerDataComponent.get(player);
                    boolean wasSquid = data.isSquid();

                    // squid form
                    boolean nowSquid = wasSquid;
                    switch (ClientConfig.INSTANCE.changeSquidKeyBehavior.getValue()) {
                        case TOGGLE -> nowSquid = CHANGE_SQUID_FORM.wasToggled() ? !wasSquid : nowSquid;
                        case HOLD -> nowSquid = CHANGE_SQUID_FORM.isPressed();
                    }

                    if (nowSquid) nowSquid = SplatcraftUtil.canSquid(player);
                    if (wasSquid != nowSquid) NetworkingClient.sendKeyChangeSquidForm(nowSquid);
                }
            }
        });
    }

    private static <K extends KeyBinding> K register(String id, int code, KeyBindingFactory<K> factory) {
        Identifier identifier = new Identifier(Splatcraft.MOD_ID, id);
        String dottedIdentifier = "%s.%s".formatted(identifier.getNamespace(), identifier.getPath());
        K key = factory.create("key.%s".formatted(dottedIdentifier), InputUtil.Type.KEYSYM, code, KEY_CATEGORY);
        KeyBindingHelper.registerKeyBinding(key);
        return key;
    }

    @SuppressWarnings("unchecked")
    private static <K extends KeyBinding> K register(String id, int code) {
        return (K) register(id, code, KeyBinding::new);
    }

    @FunctionalInterface
    public interface KeyBindingFactory<K extends KeyBinding> {
        K create(String translationKey, InputUtil.Type type, int code, String category);
    }
}
