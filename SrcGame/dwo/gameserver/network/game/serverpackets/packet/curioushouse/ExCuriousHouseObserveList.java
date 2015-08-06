package dwo.gameserver.network.game.serverpackets.packet.curioushouse;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.scripts.instances.ChaosFestival;
import javolution.util.FastList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Shows list of available Chaos Festival Battles for request from RequestObservingListCuriousHouse.
 * @author ANZO
 * @author Yorie
 */
public class ExCuriousHouseObserveList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		List<ChaosFestival.ChaosFestivalWorld> worlds = ChaosFestival.getInstance().getActiveBattleWorlds();
		List<ChaosFestival.ChaosFestivalWorld> filtered = worlds.stream().filter(world -> world.status == 1).collect(Collectors.toCollection(FastList::new));

		writeD(worlds.size());
		int index = -1;
		for(ChaosFestival.ChaosFestivalWorld world : filtered)
		{
			writeD(++index); // номер строки от 0
			writeS("");   // HouseName в клиенте закоменчино ( приходит Room1 2 3 и тд )
			// HouseName = GetSystemString(2806) $ ( ID + 1 ) ;
			writeH(world.status != 1 ? 0x00 : 0x01); // State    if (State == 0 ) StateString = 1718; else  StateString = 1719;
			// 1719	u,В игре\0     1718	u,Ожидание\0
			writeD(world.allowed.size()); // число участников
			// конец массива
		}
	}
}