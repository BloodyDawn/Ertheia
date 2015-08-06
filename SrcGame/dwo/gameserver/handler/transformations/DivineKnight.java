package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DivineKnight extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 680, 681, 682, 683, 684, 685, 795, 796};

	public DivineKnight()
	{
		// id, colRadius, colHeight
		super(252, 16, 30);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(680, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(681, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(682, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(683, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(684, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(685, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(795, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(796, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(680, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(681, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(682, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(683, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(684, 1), false, false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(685, 1), false, false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(795, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(796, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
