package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.12.12
 * Time: 0:11
 */
public class GolemSiege extends L2Transformation
{
	private static final int[] Skills = {619, 15570, 15571, 15573, 15574};

	public GolemSiege()
	{
		// id, colRadius, colHeight
		super(148, 50.00, 68.00);
	}

	@Override
	public void transformedSkills()
	{
		// http://bladensoul.ru/scrupload/i/1d051c.png
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(15570, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(15571, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(15573, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(15574, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(15570, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(15571, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(15573, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(15574, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
