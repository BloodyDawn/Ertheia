package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class VanguardPaladin extends L2Transformation
{
	private static final int[] Skills1 = {18, 28, 196, 197, 293, 400, 406, 814, 816, 838, 956, 957};
	private static final int[] Skills2 = {18, 28, 196, 197, 400, 406, 838};

	public VanguardPaladin()
	{
		// id, colRadius, colHeight
		super(312, 8, 23);
	}

	@Override
	public void transformedSkills()
	{
		L2PcInstance player = getPlayer();
		if(player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;

			// Two handed mastery
			player.addSkill(SkillTable.getInstance().getInfo(293, level), false);
			// Full Swing
			player.addSkill(SkillTable.getInstance().getInfo(814, level), false);
			// Power Divide
			player.addSkill(SkillTable.getInstance().getInfo(816, level), false);
			// Boost Morale
			player.addSkill(SkillTable.getInstance().getInfo(956, level), false);
			// Guillotine Attack
			player.addSkill(SkillTable.getInstance().getInfo(957, level), false);
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
			// Two handed mastery
			player.removeSkill(SkillTable.getInstance().getInfo(293, level), false);
			// Full Swing
			player.removeSkill(SkillTable.getInstance().getInfo(814, level), false);
			// Power Divide
			player.removeSkill(SkillTable.getInstance().getInfo(816, level), false);
			// Switch Stance
			player.removeSkill(SkillTable.getInstance().getInfo(838, 1), false);
			// Boost Morale
			player.removeSkill(SkillTable.getInstance().getInfo(956, level), false, false);
			// Guillotine Attack
			player.removeSkill(SkillTable.getInstance().getInfo(957, level), false);
		}
		player.setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
