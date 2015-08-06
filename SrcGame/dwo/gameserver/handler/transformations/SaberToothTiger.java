package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class SaberToothTiger extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 746, 747, 748};

	public SaberToothTiger()
	{
		// id, colRadius, colHeight
		super(5, 34, 28);
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
			getPlayer().addSkill(SkillTable.getInstance().getInfo(746, 3), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(747, 3), false);
		}
		else if(getPlayer().getLevel() >= 73)
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(746, 2), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(747, 2), false);
		}
		else if(getPlayer().getLevel() >= 70)
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(746, 1), false);
			getPlayer().addSkill(SkillTable.getInstance().getInfo(747, 1), false);
		}
		getPlayer().addSkill(SkillTable.getInstance().getInfo(748, 1), false);
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
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(746, 3), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(747, 3), false);
		}
		else if(getPlayer().getLevel() >= 73)
		{
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(746, 2), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(747, 2), false);
		}
		else
		{
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(746, 1), false);
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(747, 1), false);
		}
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(748, 1), false, false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
