package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DwarfGolem extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 806, 807, 808, 809};

	public DwarfGolem()
	{
		// id, colRadius, colHeight
		super(259, 31, 51.8);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(806, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(807, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(808, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(809, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(806, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(807, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(808, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(809, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
