package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;

public class Zariche extends L2Transformation
{
	public Zariche()
	{
		// id, colRadius, colHeight
		super(301, 12, 31.58);
	}

	@Override
	public void onTransform()
	{
		// Set charachter name to transformed name
		getPlayer().getAppearance().setVisibleName("Зариче");
		getPlayer().getAppearance().setVisibleTitle("");
	}

	@Override
	public void onUntransform()
	{
		// set character back to true name.
		getPlayer().getAppearance().setVisibleName(null);
		getPlayer().getAppearance().setVisibleTitle(null);
	}
}
