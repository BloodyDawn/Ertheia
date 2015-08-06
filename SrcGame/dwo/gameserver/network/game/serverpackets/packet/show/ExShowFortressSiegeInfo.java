package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager.SiegeSpawn;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

/**
 * @author KenM
 */

public class ExShowFortressSiegeInfo extends L2GameServerPacket
{
	private int _fortId;
	private int _size;
	private Fort _fort;
	private int _csize;
	private int _csize2;

	public ExShowFortressSiegeInfo(Fort fort)
	{
		_fort = fort;
		_fortId = fort.getFortId();
		_size = fort.getFortSize();
		FastList<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortId);
		if(commanders != null)
		{
			_csize = commanders.size();
		}
		_csize2 = _fort.getSiege().getCommanders().size();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_fortId); // Fortress Id
		writeD(_size); // Total Barracks Count
		if(_csize > 0)
		{
			switch(_csize)
			{
				case 3:
					switch(_csize2)
					{
						case 0:
							writeD(0x03);
							break;
						case 1:
							writeD(0x02);
							break;
						case 2:
							writeD(0x01);
							break;
						case 3:
							writeD(0x00);
							break;
					}
					break;
				case 4: // TODO: change 4 to 5 once control room supported
					switch(_csize2)
					// TODO: once control room supported, update writeD(0x0x) to support 5th room
					{
						case 0:
							writeD(0x05);
							break;
						case 1:
							writeD(0x04);
							break;
						case 2:
							writeD(0x03);
							break;
						case 3:
							writeD(0x02);
							break;
						case 4:
							writeD(0x01);
							break;
					}
					break;
			}
		}
		else
		{
			for(int i = 0; i < _size; i++)
			{
				writeD(0x00);
			}
		}
	}
}
