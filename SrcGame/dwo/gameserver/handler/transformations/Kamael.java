package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Kamael extends L2Transformation
{
	private static final int[] Skills = {619, 539, 540, 1471};

	public Kamael()
	{
		// id, colRadius, colHeight
		super(251, 10, 32.76);
	}

	@Override
	public void transformedSkills()
	{
		// Nail Attack
		getPlayer().addSkill(SkillTable.getInstance().getInfo(539, 1), false);
		// Wing Assault
		getPlayer().addSkill(SkillTable.getInstance().getInfo(540, 1), false);
		// Soul Sucking
		getPlayer().addSkill(SkillTable.getInstance().getInfo(1471, 1), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Nail Attack
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(539, 1), false);
		// Wing Assault
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(540, 1), false);
		// Soul Sucking
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(1471, 1), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
