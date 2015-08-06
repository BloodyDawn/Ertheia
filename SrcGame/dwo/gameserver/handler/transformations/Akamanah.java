package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;

public class Akamanah extends L2Transformation
{
	public Akamanah()
	{
		// id, colRadius, colHeight
		super(302, 10, 32.73);
	}

	@Override
	public void onTransform()
	{
		// Set charachter name to transformed name
		getPlayer().getAppearance().setVisibleName("Акаманах");
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
