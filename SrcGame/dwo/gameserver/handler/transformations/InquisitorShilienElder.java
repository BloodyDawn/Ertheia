package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class InquisitorShilienElder extends L2Transformation
{
	private static final int[] Skills1 = {838, 5491, 1523, 1528, 1524, 1525, 1430, 1303, 1059, 1043};
	private static final int[] Skills2 = {838, 5491, 1430, 1303, 1059};

	public InquisitorShilienElder()
	{
		// id, colRadius, colHeight
		super(318, 8, 25);
	}

	@Override
	public void transformedSkills()
	{
		int lvl = getPlayer().getLevel() - 43;
		if(lvl < 1)
		{
			lvl = 1;
		}
		if(getPlayer().getLevel() > 39)
		{
			// Divine Punishment
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1523, lvl), false);
			// Divine Flash
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1528, lvl), false);
			// Holy Weapon
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1043, 1), false);
			// Surrender to the Holy
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1524, lvl), false);
			// Divine Curse
			getPlayer().addSkill(SkillTable.getInstance().getInfo(1525, lvl), false);
			getPlayer().setTransformAllowedSkills(Skills1);
		}
		else
		{
			getPlayer().setTransformAllowedSkills(Skills2);
		}
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Switch Stance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(838, 1), false);
	}

	@Override
	public void removeSkills()
	{
		// Divine Punishment
		getPlayer().removeSkill(1523, false);
		// Divine Flash
		getPlayer().removeSkill(1528, false);
		// Holy Weapon
		getPlayer().removeSkill(1043, false);
		// Surrender to the Holy
		getPlayer().removeSkill(1524, false);
		// Divine Curse
		getPlayer().removeSkill(1525, false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(5491, false);
		// Switch Stance
		getPlayer().removeSkill(838, false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
