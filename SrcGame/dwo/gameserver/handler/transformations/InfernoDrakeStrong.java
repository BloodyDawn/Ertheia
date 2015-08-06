package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class InfernoDrakeStrong extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 576, 577, 578, 579};

	public InfernoDrakeStrong()
	{
		// id, colRadius, colHeight
		super(213, 15, 24);
	}

	@Override
	public void transformedSkills()
	{
		// Paw Strike
		getPlayer().addSkill(SkillTable.getInstance().getInfo(576, 4), false);
		// Fire Breath
		getPlayer().addSkill(SkillTable.getInstance().getInfo(577, 4), false);
		// Blaze Quake
		getPlayer().addSkill(SkillTable.getInstance().getInfo(578, 4), false);
		// Fire Armor
		getPlayer().addSkill(SkillTable.getInstance().getInfo(579, 4), false);
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
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(576, 4), false);
		// Fire Breath
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(577, 4), false);
		// Blaze Quake
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(578, 4), false);
		// Fire Armor
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(579, 4), false, false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
