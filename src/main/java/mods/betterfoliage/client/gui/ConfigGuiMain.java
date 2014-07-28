package mods.betterfoliage.client.gui;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiMain extends ConfigGuiScreenBase {

	public enum Button {CLOSE, 
						TOGGLE_LEAVES, CONFIG_LEAVES,
						TOGGLE_GRASS, CONFIG_GRASS,
						TOGGLE_CACTUS, CONFIG_CACTUS,
						TOGGLE_LILYPAD, CONFIG_LILYPAD,
						TOGGLE_REED, CONFIG_REED,
						TOGGLE_ALGAE, CONFIG_ALGAE,
						TOGGLE_CORAL, CONFIG_CORAL}
	
	public ConfigGuiMain(GuiScreen parent) {
		super(parent);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addButtons(int x, int y) {
		buttonList.add(new GuiButton(Button.CLOSE.ordinal(), x - 50, y + 110, 100, 20, I18n.format("message.betterfoliage.close")));
		
		buttonList.add(new GuiButton(Button.TOGGLE_LEAVES.ordinal(), x - 100, y - 100, 150, 20, ""));
		buttonList.add(new GuiButton(Button.CONFIG_LEAVES.ordinal(), x + 60, y - 100, 40, 20, I18n.format("message.betterfoliage.config")));
		
		buttonList.add(new GuiButton(Button.TOGGLE_GRASS.ordinal(), x - 100, y - 70, 150, 20, ""));
		buttonList.add(new GuiButton(Button.CONFIG_GRASS.ordinal(), x + 60, y - 70, 40, 20, I18n.format("message.betterfoliage.config")));
		
		buttonList.add(new GuiButton(Button.TOGGLE_CACTUS.ordinal(), x - 100, y - 40, 150, 20, ""));
		buttonList.add(new GuiButton(Button.CONFIG_CACTUS.ordinal(), x + 60, y - 40, 40, 20, I18n.format("message.betterfoliage.config")));
		
		buttonList.add(new GuiButton(Button.TOGGLE_LILYPAD.ordinal(), x - 100, y - 10, 150, 20, ""));
		buttonList.add(new GuiButton(Button.CONFIG_LILYPAD.ordinal(), x + 60, y - 10, 40, 20, I18n.format("message.betterfoliage.config")));
		
		buttonList.add(new GuiButton(Button.TOGGLE_REED.ordinal(), x - 100, y + 20, 150, 20, ""));
		buttonList.add(new GuiButton(Button.CONFIG_REED.ordinal(), x + 60, y + 20, 40, 20, I18n.format("message.betterfoliage.config")));
		
		buttonList.add(new GuiButton(Button.TOGGLE_ALGAE.ordinal(), x - 100, y + 50, 150, 20, ""));
		buttonList.add(new GuiButton(Button.CONFIG_ALGAE.ordinal(), x + 60, y + 50, 40, 20, I18n.format("message.betterfoliage.config")));
		
		buttonList.add(new GuiButton(Button.TOGGLE_CORAL.ordinal(), x - 100, y + 80, 150, 20, ""));
		buttonList.add(new GuiButton(Button.CONFIG_CORAL.ordinal(), x + 60, y + 80, 40, 20, I18n.format("message.betterfoliage.config")));
	}

	protected void updateButtons() {
		setButtonOptionBoolean(Button.TOGGLE_LEAVES.ordinal(), "message.betterfoliage.betterLeaves", BetterFoliage.config.leavesEnabled);
		setButtonOptionBoolean(Button.TOGGLE_GRASS.ordinal(), "message.betterfoliage.betterGrass", BetterFoliage.config.grassEnabled);
		setButtonOptionBoolean(Button.TOGGLE_CACTUS.ordinal(), "message.betterfoliage.betterCactus", BetterFoliage.config.cactusEnabled);
		setButtonOptionBoolean(Button.TOGGLE_LILYPAD.ordinal(), "message.betterfoliage.betterLilypad", BetterFoliage.config.lilypadEnabled);
		setButtonOptionBoolean(Button.TOGGLE_REED.ordinal(), "message.betterfoliage.betterReed", BetterFoliage.config.reedEnabled);
		setButtonOptionBoolean(Button.TOGGLE_ALGAE.ordinal(), "message.betterfoliage.betterAlgae", BetterFoliage.config.algaeEnabled);
		setButtonOptionBoolean(Button.TOGGLE_CORAL.ordinal(), "message.betterfoliage.betterCoral", BetterFoliage.config.coralEnabled);
		((GuiButton) buttonList.get(Button.CONFIG_CACTUS.ordinal())).enabled = false;
	}
	
	@Override
	protected void onButtonPress(int id) {
		if (id == Button.CLOSE.ordinal()) {
			BetterFoliage.config.save();
			Minecraft.getMinecraft().renderGlobal.loadRenderers();
			FMLClientHandler.instance().showGuiScreen(parent);
		}
		if (id == Button.TOGGLE_LEAVES.ordinal()) BetterFoliage.config.leavesEnabled = !BetterFoliage.config.leavesEnabled;
		if (id == Button.TOGGLE_GRASS.ordinal()) BetterFoliage.config.grassEnabled = !BetterFoliage.config.grassEnabled;
		if (id == Button.TOGGLE_CACTUS.ordinal()) BetterFoliage.config.cactusEnabled = !BetterFoliage.config.cactusEnabled;
		if (id == Button.TOGGLE_LILYPAD.ordinal()) BetterFoliage.config.lilypadEnabled = !BetterFoliage.config.lilypadEnabled;
		if (id == Button.TOGGLE_REED.ordinal()) BetterFoliage.config.reedEnabled = !BetterFoliage.config.reedEnabled;
		if (id == Button.TOGGLE_ALGAE.ordinal()) BetterFoliage.config.algaeEnabled = !BetterFoliage.config.algaeEnabled;
		if (id == Button.TOGGLE_CORAL.ordinal()) BetterFoliage.config.coralEnabled = !BetterFoliage.config.coralEnabled;
		
		if (id== Button.CONFIG_LEAVES.ordinal()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiLeaves(this));
		if (id== Button.CONFIG_GRASS.ordinal()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiGrass(this));
		if (id== Button.CONFIG_LILYPAD.ordinal()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiLilypad(this));
		if (id== Button.CONFIG_REED.ordinal()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiReed(this));
		if (id== Button.CONFIG_ALGAE.ordinal()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiAlgae(this));
		if (id== Button.CONFIG_CORAL.ordinal()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiCoral(this));
	}

}
