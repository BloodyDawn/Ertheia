package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;

/**
 * * @author Gnacik
 * *
 */
public class L2EventChestInstance extends L2EventMonsterInstance
{
	private boolean _isVisible;
	private boolean _isTriggered;

	public L2EventChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		setIsNoRndWalk(true);
		disableCoreAI(true);

		eventSetDropOnGround(true);
		eventSetBlockOffensiveSkills(true);
	}

	public boolean canSee(L2Character cha)
	{
		if(cha == null)
		{
			return false;
		}
		if(cha.isGM())
		{
			return true;
		}
		return _isVisible;
	}

	public void trigger()
	{
		_isTriggered = true;
		broadcastPacket(new NpcInfo(this));
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if(_isTriggered || canSee(activeChar))
		{
			activeChar.sendPacket(new NpcInfo(this));
		}
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		getKnownList().getKnownPlayers().values().stream().filter(player -> player != null && (_isTriggered || canSee(player))).forEach(player -> player.sendPacket(mov));
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if(player == null)
			{
				continue;
			}
			if(isInsideRadius(player, radiusInKnownlist, false, false))
			{
				if(_isTriggered || canSee(player))
				{
					player.sendPacket(mov);
				}
			}
		}
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !canSee(attacker);
	}
}