package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class EpicQuestChild extends L2Transformation
{
	private static final int[] SKILLS = {5437, 960};

	public EpicQuestChild()
	{
		// id, colRadius, colHeight
		super(112, 5, 12.3);
	}

	@Override
	public void transformedSkills()
	{
		// Dissonance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5437, 1), false);
		// Race Running
		getPlayer().addSkill(SkillTable.getInstance().getInfo(960, 1), false);
		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void removeSkills()
	{
		// Dissonance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5437, 1), false);
		// Race Running
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(960, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
