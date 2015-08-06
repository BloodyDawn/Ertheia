package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class ValeMaster extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 742, 743, 744, 745};

	public ValeMaster()
	{
		// id, colRadius, colHeight
		super(4, 12, 40);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		if(getPlayer().getLevel() >= 76)
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(742, 3), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(743, 3), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(744, 3), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(745, 3), false);
		}
		else if(getPlayer().getLevel() >= 73)
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(742, 2), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(743, 2), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(744, 2), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(745, 2), false);
		}
		else if(getPlayer().getLevel() >= 70)
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(742, 1), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(743, 1), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(744, 1), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(745, 1), false);
		}
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		if(getPlayer().getLevel() >= 76)
		{
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(742, 3), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(743, 3), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(744, 3), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(745, 3), false);
		}
		else if(getPlayer().getLevel() >= 73)
		{
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(742, 2), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(743, 2), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(744, 2), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(745, 2), false);
		}
		else
		{
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(742, 1), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(743, 1), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(744, 1), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(745, 1), false);
		}
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
