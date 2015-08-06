package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Unicorniun extends L2Transformation
{
	private static final int[] Skills = {906, 907, 908, 909, 910, 8248};

	public Unicorniun()
	{
		// id, colRadius, colHeight
		super(220, 8, 30);
	}

	@Override
	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Lance Step (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(906, 1), false);
		// Aqua Blast (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(907, 1), false);
		// Spin Slash (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(908, 1), false);
		// Ice Focus (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(909, 1), false);
		// Water Jet (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(910, 1), false);
		// Transfrom Dispel (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Lance Step
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(906, 1), false);
		// Aqua Blast
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(907, 1), false);
		// Spin Slash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(908, 1), false);
		// Ice Focus
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(909, 1), false);
		// Water Jet
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(910, 1), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
