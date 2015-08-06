package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class VanguardDarkAvenger extends L2Transformation
{
	private static final int[] Skills1 = {18, 28, 65, 86, 144, 283, 815, 817, 838, 956, 958, 401};
	private static final int[] Skills2 = {18, 28, 65, 86, 283, 401, 838};

	public VanguardDarkAvenger()
	{
		// id, colRadius, colHeight
		super(313, 8, 23);
	}

	@Override
	public void transformedSkills()
	{
		L2PcInstance player = getPlayer();
		if(player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;

			// Dual Weapon Mastery
			player.addSkill(SkillTable.getInstance().getInfo(144, level), false);
			// Blade Hurricane
			player.addSkill(SkillTable.getInstance().getInfo(815, level), false);
			// Double Strike
			player.addSkill(SkillTable.getInstance().getInfo(817, level), false);
			// Boost Morale
			player.addSkill(SkillTable.getInstance().getInfo(956, level), false);
			// Triple Blade Slash
			player.addSkill(SkillTable.getInstance().getInfo(958, level), false);
			player.setTransformAllowedSkills(Skills1);
		}
		else
		{
			player.setTransformAllowedSkills(Skills2);
		}

		// Switch Stance
		player.addSkill(SkillTable.getInstance().getInfo(838, 1), false);
	}

	@Override
	public void removeSkills()
	{
		L2PcInstance player = getPlayer();
		int level = player.getLevel() - 43;

		if(level > 0)
		{
			// Dual Weapon Mastery
			player.removeSkill(SkillTable.getInstance().getInfo(144, level), false);
			// Blade Hurricane
			player.removeSkill(SkillTable.getInstance().getInfo(815, level), false);
			// Double Strike
			player.removeSkill(SkillTable.getInstance().getInfo(817, level), false);
			// Switch Stance
			player.removeSkill(SkillTable.getInstance().getInfo(838, 1), false);
			// Boost Morale
			player.removeSkill(SkillTable.getInstance().getInfo(956, level), false, false);
			// Triple Blade Slash
			player.removeSkill(SkillTable.getInstance().getInfo(958, level), false);
		}

		player.setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
