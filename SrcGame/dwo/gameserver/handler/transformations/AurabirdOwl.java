package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class AurabirdOwl extends L2Transformation
{
	private static final int[] Skills = {619, 884, 885, 887, 889, 892, 893, 895, 911, 932};

	public AurabirdOwl()
	{
		// id, colRadius, colHeight
		super(9, 40, 18.57);
	}

	@Override
	public void transformedSkills()
	{
		getPlayer().setIsFlyingMounted(true);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Air Blink
		if(getPlayer().getLevel() >= 75)
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(885, 1), false);
		}

		// Exhilarate
		if(getPlayer().getLevel() >= 83)
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(895, 1), false);
		}
		int lvl = getPlayer().getLevel() - 74;
		if(lvl > 0)
		{
			// Air Assault (up to 11 levels)
			getPlayer().addSkill(SkillTable.getInstance().getInfo(884, lvl), false);
			// Sky Clutch (up to 11 levels)
			getPlayer().addSkill(SkillTable.getInstance().getInfo(887, lvl), false);
			// Energy Storm (up to 11 levels)
			getPlayer().addSkill(SkillTable.getInstance().getInfo(889, lvl), false);
			// Energy Shot (up to 11 levels)
			getPlayer().addSkill(SkillTable.getInstance().getInfo(892, lvl), false);
			// Concentrated Energy Shot (up to 11 levels)
			getPlayer().addSkill(SkillTable.getInstance().getInfo(893, lvl), false);
			// Energy Burst (up to 11 levels)
			getPlayer().addSkill(SkillTable.getInstance().getInfo(911, lvl), false);
		}
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		getPlayer().setIsFlyingMounted(false);
		// Air Blink
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(885, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(895, 1), false);
		int lvl = getPlayer().getLevel() - 74;
		if(lvl > 0)
		{
			// Air Assault (up to 11 levels)
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(884, lvl), false);
			// Sky Clutch (up to 11 levels)
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(887, lvl), false);
			// Energy Storm (up to 11 levels)
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(889, lvl), false);
			// Energy Shot (up to 11 levels)
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(892, lvl), false);
			// Concentrated Energy Shot (up to 11 levels)
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(893, lvl), false);
			// Energy Burst (up to 11 levels)
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(911, lvl), false);
		}
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
