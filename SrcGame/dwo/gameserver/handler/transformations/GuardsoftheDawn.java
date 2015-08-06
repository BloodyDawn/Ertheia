package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class GuardsoftheDawn extends L2Transformation
{
	private static final int[] Skills = {963, 5491};

	public GuardsoftheDawn()
	{
		// id, colRadius, colHeight
		super(113, 8, 23.5);
	}

	@Override
	public void transformedSkills()
	{
		getPlayer().getInventory().blockAllItems();
		getPlayer().addSkill(SkillTable.getInstance().getInfo(963, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		getPlayer().getInventory().unblock();
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(963, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}

	@Override
	public boolean canDoMeleeAttack()
	{
		return false;
	}
}