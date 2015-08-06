package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Gordon extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 728, 729, 730};

	public Gordon()
	{
		// id, colRadius, colHeight
		super(308, 43, 46.6);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(728, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(729, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(730, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(728, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(729, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(730, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
