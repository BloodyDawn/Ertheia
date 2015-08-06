package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.InstanceHistoryManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.holders.CharacterClassHolder;
import dwo.gameserver.model.holders.InstancePartyHistoryHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

public class ExLoadInzonePartyHistory extends L2GameServerPacket
{
	private int _charId;

	public ExLoadInzonePartyHistory(int charId)
	{
		_charId = charId;
	}

	@Override
	protected void writeImpl()
	{
		List<Integer> partyIds = InstanceHistoryManager.getInstance().getCharacterInzoneHistory(_charId);
		InstancePartyHistoryHolder inzoneHolder;
		writeD(partyIds.size()); // Количество историй
		if(!partyIds.isEmpty())
		{
			for(Integer partyId : partyIds)
			{
				inzoneHolder = InstanceHistoryManager.getInstance().getPartyInzoneHistory(partyId);
				for(CharacterClassHolder charHolder : inzoneHolder.getCharsInParty())
				{
					writeD(partyId); // ид группы ( любой но уникальный по нему формируется лист скролов )
					writeD(inzoneHolder.getInstanceId()); // Ид иста
					writeD(inzoneHolder.getInstanceUseTime()); // время захода в инст
					writeS(CharNameTable.getInstance().getNameById(charHolder.getCharId())); // Имя сопартийца
					writeH(charHolder.getCharClassId()); // ид Профы сопартийца
					writeH(WorldManager.getInstance().getPlayer(charHolder.getCharId()) == null ? 0 : 1); // 0 - нет в игре  1 - есть в игре
				}
			}
		}
	}
}
