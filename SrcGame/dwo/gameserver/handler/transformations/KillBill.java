package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class KillBill extends L2Transformation
{
	private static final int[] SKILLS = {20003, 20004, 20005, 619};

	public KillBill()
	{
		// id, colRadius, colHeight
		super(20006, 25, 19);
	}

	@Override
	public void transformedSkills()
	{
		if(getPlayer().getTransformationId() != 20006 || getPlayer().isCursedWeaponEquipped())
		{
			return;
		}

		// Dragon Kick
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20003, 1), false);

		// Dragon Dash
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20004, 1), false);

		// Dragon Aura
		getPlayer().addSkill(SkillTable.getInstance().getInfo(20005, 1), false);

		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void removeSkills()
	{
		// Dragon Slash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20003, 1), false);

		// Dragon Dash
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20004, 1), false);

		// Dragon Aura
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(20005, 1), false);

		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
