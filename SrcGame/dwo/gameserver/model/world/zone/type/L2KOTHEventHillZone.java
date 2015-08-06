package dwo.gameserver.model.world.zone.type;

import dwo.config.events.ConfigEventKOTH;
import dwo.gameserver.instancemanager.events.KOTH.KOTHEvent;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.L2ZoneType;

public class L2KOTHEventHillZone extends L2ZoneType
{
	public L2KOTHEventHillZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(ConfigEventKOTH.KOTH_EVENT_ENABLED && KOTHEvent.isStarted())
		{
			if(!(character instanceof L2PcInstance))
			{
				return;
			}
			L2PcInstance player = character.getActingPlayer();
			if(KOTHEvent.getParticipantTeamId(player.getObjectId()) == -1)
			{
				return;
			}
			if(KOTHEvent.addPlayerOnTheHill(player))
			{
				player.sendMessage("Царь Горы: Вы забрались на гору!");
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(ConfigEventKOTH.KOTH_EVENT_ENABLED && KOTHEvent.isStarted())
		{
			if(!(character instanceof L2PcInstance))
			{
				return;
			}
			L2PcInstance player = character.getActingPlayer();
			if(KOTHEvent.getParticipantTeamId(player.getObjectId()) == -1)
			{
				return;
			}
			if(KOTHEvent.removePlayerOnTheHill(player.getObjectId()))
			{
				player.sendMessage("Царь Горы: Вы ушли с горы!");
			}
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
		if(ConfigEventKOTH.KOTH_EVENT_ENABLED && KOTHEvent.isStarted())
		{
			if(!(character instanceof L2PcInstance))
			{
				return;
			}
			L2PcInstance player = character.getActingPlayer();
			if(KOTHEvent.getParticipantTeamId(player.getObjectId()) == -1)
			{
				return;
			}
			if(KOTHEvent.removePlayerOnTheHill(player.getObjectId()))
			{
				player.sendMessage("Царь Горы: Вы умерли на горе, ваше нахожднение на ней во время смерти не будет зачтено.");
			}
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		if(ConfigEventKOTH.KOTH_EVENT_ENABLED && KOTHEvent.isStarted())
		{
			if(!(character instanceof L2PcInstance))
			{
				return;
			}
			L2PcInstance player = character.getActingPlayer();
			if(KOTHEvent.getParticipantTeamId(player.getObjectId()) == -1)
			{
				return;
			}
			if(KOTHEvent.addPlayerOnTheHill(player))
			{
				player.sendMessage("Царь Горы: Вы были воссркешены на горе, ваше нахожднение теперь будет зачтено.");
			}
		}
	}
}
