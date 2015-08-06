package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class GrailApostleNormal extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 559, 560, 561, 562};

	public GrailApostleNormal()
	{
		// id, colRadius, colHeight
		super(202, 8, 30);
	}

	@Override
	public void transformedSkills()
	{
		// Spear
		getPlayer().addSkill(SkillTable.getInstance().getInfo(559, 3), false);
		// Power Slash
		getPlayer().addSkill(SkillTable.getInstance().getInfo(560, 3), false);
		// Bless of Angel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(561, 3), false);
		// Wind of Angel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(562, 3), false);
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
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(559, 3), false);
		// Power Slash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(560, 3), false);
		// Bless of Angel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(561, 3), false, false);
		// Wind of Angel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(562, 3), false, false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
