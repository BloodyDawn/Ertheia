package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.cache.CrestCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import org.apache.log4j.Level;

public class RequestSetAllyCrest extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length > 192)
		{
			return;
		}

		_data = new byte[_length];
		readB(_data);
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_length < 0)
		{
			activeChar.sendMessage("Ошибка передачи файла.");
			return;
		}
		if(_length > 192)
		{
			activeChar.sendMessage("Ваш значек альянса слишком большой (максимально 192 байта).");
			return;
		}

		if(activeChar.getAllyId() != 0)
		{
			L2Clan leaderclan = ClanTable.getInstance().getClan(activeChar.getAllyId());

			if(activeChar.getClanId() != leaderclan.getClanId() || !activeChar.isClanLeader())
			{
				return;
			}

			boolean remove = false;
			if(_length == 0 || _data.length == 0)
			{
				remove = true;
			}

			int newId = 0;
			if(!remove)
			{
				newId = IdFactory.getInstance().getNextId();
			}

			if(!remove && !CrestCache.getInstance().saveAllyCrest(newId, _data))
			{
				_log.log(Level.INFO, "Error saving crest for ally " + leaderclan.getAllyName() + " [" + leaderclan.getAllyId() + ']');
				return;
			}

			leaderclan.changeAllyCrest(newId, false);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 87 RequestSetAllyCrest";
	}
}
