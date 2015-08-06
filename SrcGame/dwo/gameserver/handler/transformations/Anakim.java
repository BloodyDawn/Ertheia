package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Anakim extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 720, 721, 722, 723, 724};

	public Anakim()
	{
		// id, colRadius, colHeight
		super(306, 15.5, 29);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(720, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(721, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(722, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(723, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(724, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(720, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(721, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(722, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(723, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(724, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
