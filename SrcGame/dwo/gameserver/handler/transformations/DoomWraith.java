package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DoomWraith extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 586, 587, 588, 589};

	public DoomWraith()
	{
		// id, colRadius, colHeight
		super(2, 13, 25);
	}

	@Override
	public void transformedSkills()
	{
		// Rolling Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(586, 2), false);
		// Curse of Darkness
		getPlayer().addSkill(SkillTable.getInstance().getInfo(588, 2), false);
		// Dig Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(587, 2), false);
		// Darkness Energy Drain
		getPlayer().addSkill(SkillTable.getInstance().getInfo(589, 2), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Rolling Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(586, 2), false);
		// Curse of Darkness
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(588, 2), false);
		// Dig Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(587, 2), false);
		// Darkness Energy Drain
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(589, 2), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
