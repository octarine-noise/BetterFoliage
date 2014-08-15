package mods.betterfoliage.common.config;

public class BetterFoliageConfig extends ConfigBase {
	
	@CfgElement(category="leaves", key="enabled") 
	public boolean leavesEnabled = true;
	
	@CfgElement(category="leaves", key="skewMode")
	public boolean leavesSkew = false;
	
	@CfgElement(category="grass", key="enabled")
	public boolean grassEnabled = true;
	
	@CfgElement(category="grass", key="useGenerated")
	public boolean grassUseGenerated = false;
	
	@CfgElement(category="cactus", key="enabled")
	public boolean cactusEnabled = true;
	
	@CfgElement(category="lilypad", key="enabled")
	public boolean lilypadEnabled = true;
	
	@CfgElement(category="reed", key="enabled")
	public boolean reedEnabled = true;
	
	@CfgElement(category="algae", key="enabled")
	public boolean algaeEnabled = true;
	
	@CfgElement(category="coral", key="enabled")
	public boolean coralEnabled = true;
	
	@CfgElement(category="fallingLeaves", key="enabled")
	public boolean fallingLeavesEnabled = true;
	
	@CfgElement(category="leaves", key="horizontalOffset")
	public OptionDouble leavesHOffset = new OptionDouble(0.0, 0.4, 0.025, 0.2);
	
	@CfgElement(category="leaves", key="verticalOffset")
	public OptionDouble leavesVOffset = new OptionDouble(0.0, 0.4, 0.025, 0.1);
	
	@CfgElement(category="leaves", key="size")
	public OptionDouble leavesSize = new OptionDouble(0.75, 1.8, 0.05, 1.4);
			
	@CfgElement(category="grass", key="horizontalOffset")
	public OptionDouble grassHOffset = new OptionDouble(0.0, 0.4, 0.025, 0.2);
	
	@CfgElement(category="grass", key="heightMin")
	@Limit(max="grassHeightMax")
	public OptionDouble grassHeightMin = new OptionDouble(0.1, 1.5, 0.05, 0.8);
	
	@CfgElement(category="grass", key="heightMax") 
	public OptionDouble grassHeightMax = new OptionDouble(0.1, 1.5, 0.05, 1.0);
	
	@CfgElement(category="grass", key="size")
	public OptionDouble grassSize = new OptionDouble(0.5, 1.5, 0.05, 1.0);
	
	@CfgElement(category="lilypad", key="horizontalOffset")
	public OptionDouble lilypadHOffset = new OptionDouble(0.0, 0.25, 0.025, 0.1);
	
	@CfgElement(category="lilypad", key="chance")
	public OptionInteger lilypadChance = new OptionInteger(0, 64, 1, 16);
	
	@CfgElement(category="reed", key="horizontalOffset")
	public OptionDouble reedHOffset = new OptionDouble(0.0, 0.25, 0.025, 0.1);
	
	@CfgElement(category="reed", key="heightMin")
	@Limit(max="reedHeightMax")
	public OptionDouble reedHeightMin = new OptionDouble(1.5, 3.5, 0.1, 2.0);
	
	@CfgElement(category="reed", key="heightMax")
	public OptionDouble reedHeightMax = new OptionDouble(1.5, 3.5, 0.1, 2.5);
	
	@CfgElement(category="reed", key="chance")
	public OptionInteger reedChance = new OptionInteger(0, 64, 1, 32);
	
	@CfgElement(category="algae", key="horizontalOffset")
	public OptionDouble algaeHOffset = new OptionDouble(0.0, 0.25, 0.025, 0.1);
	
	@CfgElement(category="algae", key="size")
	public OptionDouble algaeSize = new OptionDouble(0.5, 1.5, 0.05, 1.0);
	
	@CfgElement(category="algae", key="heightMin")
	@Limit(max="algaeHeightMax")
	public OptionDouble algaeHeightMin = new OptionDouble(0.1, 1.5, 0.05, 0.5);
	
	@CfgElement(category="algae", key="heightMax")
	public OptionDouble algaeHeightMax = new OptionDouble(0.1, 1.5, 0.05, 1.0);
	
	@CfgElement(category="algae", key="chance")
	public OptionInteger algaeChance = new OptionInteger(0, 64, 1, 48);
	
	@CfgElement(category="coral", key="population")
	public OptionInteger coralPopulation = new OptionInteger(0, 64, 1, 32);
	
	@CfgElement(category="coral", key="chance")
	public OptionInteger coralChance = new OptionInteger(0, 64, 1, 32);
	
	@CfgElement(category="coral", key="verticalOffset")
	public OptionDouble coralVOffset = new OptionDouble(0.0, 0.25, 0.025, 0.1);
	
	@CfgElement(category="coral", key="horizontalOffset")
	public OptionDouble coralHOffset = new OptionDouble(0.0, 0.4, 0.025, 0.2);
	
	@CfgElement(category="coral", key="crustSize")
	public OptionDouble coralCrustSize = new OptionDouble(0.75, 1.75, 0.05, 1.4);
	
	@CfgElement(category="coral", key="size")
	public OptionDouble coralSize = new OptionDouble(0.25, 1.0, 0.05, 0.7);
	
	@CfgElement(category="fallingLeaves", key="speed")
	public OptionDouble fallingLeavesSpeed = new OptionDouble(0.01, 0.15, 0.01, 0.05);
	
	@CfgElement(category="fallingLeaves", key="windStrength")
	public OptionDouble fallingLeavesWindStrength = new OptionDouble(0.1, 2.0, 0.1, 0.5);
	
	@CfgElement(category="fallingLeaves", key="stormStrength")
	public OptionDouble fallingLeavesStormStrength = new OptionDouble(0.1, 2.0, 0.1, 0.8);
	
	@CfgElement(category="fallingLeaves", key="size")
	public OptionDouble fallingLeavesSize = new OptionDouble(0.25, 1.5, 0.05, 0.75);
	
	@CfgElement(category="fallingLeaves", key="chance")
	public OptionDouble fallingLeavesChance = new OptionDouble(0.005, 1.0, 0.005, 0.05);
	
	@CfgElement(category="fallingLeaves", key="perturbation")
	public OptionDouble fallingLeavesPerturb = new OptionDouble(0.05, 1.0, 0.05, 0.25);
	
	@CfgElement(category="fallingLeaves", key="lifetime")
	public OptionDouble fallingLeavesLifetime = new OptionDouble(1.0, 10.0, 0.1, 5.0);
}
