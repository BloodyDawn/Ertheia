package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class FlyingFinalForm extends L2Transformation
{
	private static final int[] Skills = {619, 932, 950, 951, 953, 1544, 1545};

	public FlyingFinalForm()
	{
		// id, colRadius, colHeight
		super(260, 9, 38);
	}

	@Override
	public void transformedSkills()
	{
		getPlayer().setIsFlyingMounted(true);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		getPlayer().setIsFlyingMounted(false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
