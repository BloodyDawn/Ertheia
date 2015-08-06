package dwo.gameserver.model.actor.instance;

import dwo.gameserver.instancemanager.HeroManager;
import dwo.gameserver.instancemanager.RaidBossPointsManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;

public class L2GrandBossInstance extends L2MonsterInstance
{
	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
	private boolean _useRaidCurse = true;

	/**
	 * Constructor for L2GrandBossInstance. This represent all grandbosses.
	 *
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsRaid(true);
		setLethalable(false);
	}

	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}
		L2PcInstance player = null;

		if(killer instanceof L2PcInstance)
		{
			player = (L2PcInstance) killer;
		}
		else if(killer instanceof L2Summon)
		{
			player = ((L2Summon) killer).getOwner();
		}

		if(player != null)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			if(player.getParty() != null)
			{
				for(L2PcInstance member : player.getParty().getMembers())
				{
					RaidBossPointsManager.getInstance().addPoints(member, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
					if(member.isNoble())
					{
						HeroManager.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
			}
			else
			{
				RaidBossPointsManager.getInstance().addPoints(player, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
				if(player.isNoble())
				{
					HeroManager.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
			}
		}
		return true;
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return BOSS_MAINTENANCE_INTERVAL;
	}

	@Override
	public float getVitalityPoints(int damage)
	{
		return -super.getVitalityPoints(damage) / 100;
	}

	@Override
	public boolean useVitalityRate()
	{
		return false;
	}

	public void setUseRaidCurse(boolean val)
	{
		_useRaidCurse = val;
	}

	@Override
	public boolean giveRaidCurse()
	{
		return _useRaidCurse;
	}
}