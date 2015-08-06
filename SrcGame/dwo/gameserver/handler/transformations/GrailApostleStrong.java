package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class GrailApostleStrong extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 559, 560, 561, 562};

	public GrailApostleStrong()
	{
		// id, colRadius, colHeight
		super(201, 8, 30);
	}

	@Override
	public void transformedSkills()
	{
		// Spear
		getPlayer().addSkill(SkillTable.getInstance().getInfo(559, 4), false);
		// Power Slash
		getPlayer().addSkill(SkillTable.getInstance().getInfo(560, 4), false);
		// Bless of Angel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(561, 4), false);
		// Wind of Angel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(562, 4), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Spear
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(559, 4), false);
		// Power Slash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(560, 4), false);
		// Bless of Angel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(561, 4), false, false);
		// Wind of Angel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(562, 4), false, false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
