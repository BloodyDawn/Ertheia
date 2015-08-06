package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class TreasureHunter extends L2Transformation
{
	private static final int[] SKILLS = {619, 20000, 20001};

	public TreasureHunter()
	{
		super(127, 9, 28.29);
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
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Treasure Hunter Search
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20000, 1), false);
		// Treasure Hunter Hammer Punch
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20001, 1), false);

		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void removeSkills()
	{
		if(!getPlayer().getPets().isEmpty())
		{
			for(L2Summon pet : getPlayer().getPets())
			{
				pet.getLocationController().decay();
			}
		}

		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Treasure Hunter Search
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20000, 1), false);
		// Treasure Hunter Hammer Punch
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20001, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
