package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DemonRace extends L2Transformation
{
	private static final int[] Skills = {900, 901, 902, 903, 904, 905, 8248};

	public DemonRace()
	{
		// id, colRadius, colHeight
		super(221, 11, 27);
	}

	@Override
	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Energy Blast (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(900, 1), false);
		// Dark Strike (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(901, 1), false);
		// Bursting Flame (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(902, 1), false);
		// Stratum Explosion (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(903, 1), false);
		// Corpse Burst (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(904, 1), false);
		// Dark Detonation (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(905, 1), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Energy Blast
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(900, 1), false);
		// Dark Strike
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(901, 1), false);
		// Bursting Flame
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(902, 1), false);
		// Stratum Explosion
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(903, 1), false);
		// Corpse Burst
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(904, 1), false);
		// Dark Detonation
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(905, 1), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
