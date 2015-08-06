package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.world.zone.L2ZoneType;

/**
 * @author durgus
 */

public class L2TownZone extends L2ZoneType
{
	private int _townId;
	private int _taxById;

	public L2TownZone(int id)
	{
		super(id);
		_taxById = 0;
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch(name)
		{
			case "townId":
				_townId = Integer.parseInt(value);
				break;
			case "taxById":
				_taxById = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_TOWN, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_TOWN, false);
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	/**
	 * @return this zones town id (if any)
	 */
	public int getTownId()
	{
		return _townId;
	}

	/**
	 * @return this town zones castle id
	 */
	public int getTaxById()
	{
		return _taxById;
	}
}
