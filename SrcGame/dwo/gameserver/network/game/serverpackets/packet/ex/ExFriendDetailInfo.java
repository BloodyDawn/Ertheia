package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.RelationObjectInfo;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class ExFriendDetailInfo extends L2GameServerPacket
{
	private final String _name;
	private final boolean _isOnline;
	private final short _level;
	private final short _classId;
	private final String _clanName;
	private final String _allyName;
	private final byte _createMonths;
	private final byte _createDays;
	private final byte _loginMonths;
	private final byte _loginDays;
	private final String _note;

	public ExFriendDetailInfo(L2PcInstance activeChar, RelationObjectInfo object)
	{
		_name = object.getName();
		_isOnline = object.isOnline();
		_level = (short) object.getLevel();
		_classId = (short) object.getClassId();
		String clanName = object.getClanName();
		String allyName = object.getAllyName();

		_clanName = clanName != null && !clanName.isEmpty() ? clanName : "-";

		_allyName = allyName != null && !allyName.isEmpty() ? allyName : "-";

		int createDays = (int) Math.ceil((System.currentTimeMillis() - object.getCreateTime()) / (double) (1000 * 60 * 60 * 24));
		int loginDays = (int) Math.ceil((System.currentTimeMillis() - object.getLoginTime()) / (double) (1000 * 60 * 60 * 24));
		_createMonths = (byte) (createDays / 30);
		_createDays = (byte) (createDays % 30);
		_loginMonths = (byte) (loginDays / 30);
		_loginDays = (byte) (loginDays % 30);
		_note = RelationListManager.getInstance().getRelationNote(activeChar.getObjectId(), object.getObjectId());
	}

	@Override
	protected void writeImpl()
	{
		writeD(0xaaa); // TODO: Unk
		writeS(_name);
		writeH(0x00);
		writeH(0x00);
		writeD(0x00); // TODO: Unk
		writeH(_level);
		writeH(_classId);
		writeD(0x71e); // TODO: Unk
		writeD(0x00);
		writeS(_clanName);
		writeQ(0x00);
		writeS(_allyName);
		writeC(_createMonths);
		writeC(_createDays);
		writeH(0x00); // TODO: Unk
		if(_isOnline)
		{
			writeH(0xffff);
		}
		else
		{
			writeC(_loginDays);
			writeC(_loginMonths);
		}
		writeH(0xffff);
		writeS(_note);
	}
}