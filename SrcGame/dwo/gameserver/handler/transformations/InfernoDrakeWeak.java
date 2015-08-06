package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class InfernoDrakeWeak extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 576, 577, 578, 579};

	public InfernoDrakeWeak()
	{
		// id, colRadius, colHeight
		super(215, 15, 24);
	}

	@Override
	public void transformedSkills()
	{
		// Paw Strike
		getPlayer().addSkill(SkillTable.getInstance().getInfo(576, 2), false);
		// Fire Breath
		getPlayer().addSkill(SkillTable.getInstance().getInfo(577, 2), false);
		// Blaze Quake
		getPlayer().addSkill(SkillTable.getInstance().getInfo(578, 2), false);
		// Fire Armor
		getPlayer().addSkill(SkillTable.getInstance().getInfo(579, 2), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Paw Strike
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(576, 2), false);
		// Fire Breath
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(577, 2), false);
		// Blaze Quake
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(578, 2), false);
		// Fire Armor
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(579, 2), false, false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
