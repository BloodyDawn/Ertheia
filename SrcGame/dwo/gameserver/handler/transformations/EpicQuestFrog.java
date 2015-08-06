package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class EpicQuestFrog extends L2Transformation
{
	private static final int[] SKILLS = {5437, 959};

	public EpicQuestFrog()
	{
		// id, colRadius, colHeight
		super(111, 20, 10);
	}

	@Override
	public void transformedSkills()
	{
		// Dissonance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5437, 1), false);
		// Frog Jump
		getPlayer().addSkill(SkillTable.getInstance().getInfo(959, 1), false);
		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void removeSkills()
	{
		// Dissonance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5437, 1), false);
		// Frog Jump
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(959, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
