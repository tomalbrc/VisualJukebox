package de.tomalbrc.visualjukebox;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new SimpleConfigScreen(parent);
    }

    static class SimpleConfigScreen extends Screen {
        private final Screen parent;

        protected SimpleConfigScreen(Screen parent) {
            super(Component.literal("My Mod Config"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int widthCenter = this.width / 2;
            int heightCenter = this.height / 2;

            this.addRenderableWidget(Button.builder(
                    Component.literal("Back"),
                btn -> this.minecraft.setScreen(parent)
            ).bounds(widthCenter - 100, heightCenter + 20, 200, 20).build());

            this.addRenderableWidget(Button.builder(
                Component.literal(ModConfig.getInstance().staticDiscs ? "Static: ON" : "Static: OFF"),
                btn -> {
                    ModConfig.getInstance().staticDiscs = !ModConfig.getInstance().staticDiscs;
                    btn.setMessage(Component.literal(ModConfig.getInstance().staticDiscs ? "Static: ON" : "Static: OFF"));
                    ModConfig.save();
                }
            ).bounds(widthCenter - 100, heightCenter - 20, 200, 20).build());
        }

        @Override
        public void onClose() {
            assert this.minecraft != null;
            this.minecraft.setScreen(parent);
        }
    }
}