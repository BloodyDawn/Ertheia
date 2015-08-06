package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class GolemGuardianNormal extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 572, 573, 574, 575};

	public GolemGuardianNormal()
	{
		// id, colRadius, colHeight
		super(211, 13, 25);
	}

	@Override
	public void transformedSkills()
	{
		// Double Slasher
		getPlayer().addSkill(SkillTable.getInstance().getInfo(572, 3), false);
		// Earthquake
		getPlayer().addSkill(SkillTable.getInstance().getInfo(573, 3), false);
		// Bomb Installation
		getPlayer().addSkill(SkillTable.getInstance().getInfo(574, 3), false);
		// Steel Cutter
		getPlayer().addSkill(SkillTable.getInstance().getInfo(575, 3), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Double Slasher
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(572, 3), false);
		// Earthquake
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(573, 3), false);
		// Bomb Installation
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(574, 3), false);
		// Steel Cutter
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(575, 3), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
