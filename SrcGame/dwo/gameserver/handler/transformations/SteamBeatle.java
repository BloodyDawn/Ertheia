package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class SteamBeatle extends L2Transformation
{
	private static final int[] Skills = {839, 5491};

	public SteamBeatle()
	{
		// id, colRadius, colHeight
		super(110, 40, 27.50);
	}

	@Override
	public void transformedSkills()
	{
		// Dismount
		getPlayer().addSkill(SkillTable.getInstance().getInfo(839, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5437, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Dismount
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(839, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5437, 1), false);// Update by rocknow
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
