package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class UnicornWeak extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 563, 564, 565, 567};

	public UnicornWeak()
	{
		// id, colRadius, colHeight
		super(206, 15, 28);
	}

	@Override
	public void transformedSkills()
	{
		// Horn of Doom
		getPlayer().addSkill(SkillTable.getInstance().getInfo(563, 2), false);
		// Gravity Control
		getPlayer().addSkill(SkillTable.getInstance().getInfo(564, 2), false);
		// Horn Assault
		getPlayer().addSkill(SkillTable.getInstance().getInfo(565, 2), false);
		// Light of Heal
		getPlayer().addSkill(SkillTable.getInstance().getInfo(567, 2), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Horn of Doom
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(563, 2), false);
		// Gravity Control
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(564, 2), false);
		// Horn Assault
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(565, 2), false);
		// Light of Heal
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(567, 2), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
