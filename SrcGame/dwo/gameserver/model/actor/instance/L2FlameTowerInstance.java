package dwo.gameserver.model.actor.instance;

import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Tower;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.zone.L2ZoneType;

import java.util.List;

/**
 * class for Flame Control Tower
 *
 * @author JIV
 */

public class L2FlameTowerInstance extends L2Tower
{
	private int _upgradeLevel;
	private List<Integer> _zoneList;

	public L2FlameTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		enableZones(false);
		return super.doDie(killer);
	}

	@Override
	public boolean onDelete()
	{
		enableZones(false);
		return super.onDelete();
	}

	public void enableZones(boolean state)
	{
		if(_zoneList != null && _upgradeLevel != 0)
		{
			int maxIndex = _upgradeLevel << 1;
			for(int i = 0; i < maxIndex; i++)
			{
				L2ZoneType zone = ZoneManager.getInstance().getZoneById(_zoneList.get(i));
				if(zone != null)
				{
					zone.setEnabled(state);
				}
			}
		}
	}

	public void setUpgradeLevel(int level)
	{
		_upgradeLevel = level;
	}

	public void setZoneList(List<Integer> list)
	{
		_zoneList = list;
		enableZones(true);
	}
}
