package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

public class StickyWidget extends AbstractWidget {

    public static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    public static final Identifier DISABLED_BUTTON = Identifier.withDefaultNamespace("widget/button_disabled");
    public static final Identifier HIGHLIGHTED_BUTTON = Identifier.withDefaultNamespace("widget/button_highlighted");
    private final Supplier<State> stateSupplier;
    private final Runnable onPress;

    public StickyWidget(int x, int y, int width, int height, Component name, Runnable onPress, Supplier<State> stateSupplier) {
        super(x, y, Math.max(width, GUIUtil.getMinimumButtonWidth(name)), Math.max(height, GUIUtil.BUTTON_HEIGHT), name);
        this.onPress = onPress;
        this.stateSupplier = stateSupplier;
    }

    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        onPress.run();
    }

    // Taken from Forge's AbstractWidget
    public int getFGColor() {
        return this.active ? 16777215 : 10526880; // White : Light Grey
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
        this.active = stateSupplier.get() == State.Up;
        this.height = Math.min(this.height, GUIUtil.BUTTON_HEIGHT);

        Minecraft minecraft = Minecraft.getInstance();
        if (stateSupplier.get() != State.Locked) {
            Identifier sprite = !this.active ? DISABLED_BUTTON : isHoveredOrFocused() ? HIGHLIGHTED_BUTTON : BUTTON;
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), this.width, this.height, ARGB.white(this.alpha));
        }

        int j = getFGColor();
        gui.centeredText(minecraft.font, this.getMessage(), getX() + this.width / 2, getY() + (this.height - 8) / 2, j | (int)Math.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }

    public enum State {
        Up,
        Down,
        Locked;

        public static State of(boolean enabled, boolean canBeEnabled) {
            if (canBeEnabled) {
                return enabled ? Down : Up;
            } else {
                return State.Locked;
            }
        }
    }

}
