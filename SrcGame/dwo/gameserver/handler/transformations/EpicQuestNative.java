package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class EpicQuestNative extends L2Transformation
{
	private static final int[] SKILLS = {5437, 961};

	public EpicQuestNative()
	{
		// id, colRadius, colHeight
		super(124, 8, 23.5);
	}

	@Override
	public void transformedSkills()
	{
		// Dissonance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5437, 1), false);
		// Swift Dash
		getPlayer().addSkill(SkillTable.getInstance().getInfo(961, 1), false);

		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void removeSkills()
	{
		// Dissonance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5437, 1), false);
		// Swift Dash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(961, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
