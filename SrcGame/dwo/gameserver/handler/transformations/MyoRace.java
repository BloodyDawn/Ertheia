package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class MyoRace extends L2Transformation
{
	private static final int[] Skills = {895, 897, 898, 899, 8248};

	public MyoRace()
	{
		// id, colRadius, colHeight
		super(219, 10, 23);
	}

	@Override
	public void transformedSkills()
	{
		if(!getPlayer().getPets().isEmpty())
		{
			for(L2Summon pet : getPlayer().getPets())
			{
				pet.getLocationController().decay();
			}
		}
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Exhilarate
		getPlayer().addSkill(SkillTable.getInstance().getInfo(895, 1), false);
		// Rolling Step (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(896, 1), false);
		// Double Blast (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(897, 1), false);
		// Tornado Slash (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(898, 1), false);
		// Cat Roar (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(899, 1), false);
		// Transfrom Dispel (up to 6)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Exhilarate
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(895, 1), false);
		// Rolling Step
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(896, 1), false);
		// Double Blast
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(897, 1), false);
		// Tornado Slash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(898, 1), false);
		// Cat Roar
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(899, 1), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
