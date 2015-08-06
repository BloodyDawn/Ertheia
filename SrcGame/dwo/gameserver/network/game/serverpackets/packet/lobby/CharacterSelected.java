package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class CharacterSelected extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private int _sessionId;

	/**
	 * @param cha Персонаж
	 *
	 * @param sessionId ID сессии
	 */
	public CharacterSelected(L2PcInstance cha, int sessionId)
	{
		_activeChar = cha;
		_sessionId = sessionId;
	}

	@Override
	protected void writeImpl()
	{
        writeS(_activeChar.getName());
        writeD(_activeChar.getObjectId()); // ??
        writeS(_activeChar.getTitle());
        writeD(_sessionId);
        writeD(_activeChar.getClanId());
        writeD(0x00); // ??
        writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
        writeD(_activeChar.getRace().ordinal());
        writeD(_activeChar.getClassId().getId());
        writeD(0x01); // active ??
        writeD(_activeChar.getX());
        writeD(_activeChar.getY());
        writeD(_activeChar.getZ());

        writeF(_activeChar.getCurrentHp());
        writeF(_activeChar.getCurrentMp());
        writeQ(_activeChar.getSp());
        writeQ(_activeChar.getExp());
        writeD(_activeChar.getLevel());
        writeD(_activeChar.getReputation());
        writeD(_activeChar.getPkKills());

        writeD(GameTimeController.getInstance().getGameTime() % (24 * 60));
        writeD(0x00);

        writeD(_activeChar.getClassId().getId());

        writeB(new byte[84]);
	}
}
