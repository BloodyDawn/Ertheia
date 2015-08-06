package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class OnyxBeast extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 584, 585};

	public OnyxBeast()
	{
		// id, colRadius, colHeight
		super(1, 14, 14.5);
	}

	@Override
	public void transformedSkills()
	{
		// Power Claw
		getPlayer().addSkill(SkillTable.getInstance().getInfo(584, 1), false);
		// Fast Moving
		getPlayer().addSkill(SkillTable.getInstance().getInfo(585, 1), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Power Claw
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(584, 1), false);
		// Fast Moving
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(585, 1), false, false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
