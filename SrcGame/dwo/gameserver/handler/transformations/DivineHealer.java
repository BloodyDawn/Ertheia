package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DivineHealer extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 698, 699, 700, 701, 702, 703};

	public DivineHealer()
	{
		// id, colRadius, colHeight
		super(255, 10, 25);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(698, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(699, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(700, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(701, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(702, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(703, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(698, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(699, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(700, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(701, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(702, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(703, 1), false, false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
