package dwo.gameserver.network.game.clientpackets.packet.CuriousHouse;

import dwo.gameserver.model.actor.controller.player.ObserverController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.scripts.instances.ChaosFestival;

import java.util.List;

/**
 * Requests for a entering Chaos Festival room in observer mode.
 * @author Bacek
 * @author Yorie
 */
public class RequestObservingCuriousHouse extends L2GameClientPacket
{
	int _room;

	// Ответ на выбор в пакете ExCuriousHouseObserveList
	@Override
	protected void readImpl()
	{
		_room = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		List<ChaosFestival.ChaosFestivalWorld> worlds = ChaosFestival.getInstance().getActiveBattleWorlds();

		if(_room > worlds.size() - 1)
		{
			return;
		}

		ChaosFestival.ChaosFestivalWorld world = worlds.get(_room);
		if(world.observerTeleportLoc == null)
		{
			return;
		}

		activeChar.getObserverController().enter(world.observerTeleportLoc, ObserverController.ObserveType.CHAOS_FESTIVAL, world.instanceId);
	}

	@Override
	public String getType()
	{
		return "[C] D0:C7 RequestObservingCuriousHouse";
	}
}
