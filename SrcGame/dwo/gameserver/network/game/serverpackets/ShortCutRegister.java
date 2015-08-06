package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.player.L2ShortCut;

public class ShortCutRegister extends L2GameServerPacket
{
	private L2ShortCut _shortcut;

	public ShortCutRegister(L2ShortCut shortcut)
	{
		_shortcut = shortcut;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_shortcut.getType().ordinal());
		writeD(_shortcut.getSlot() + _shortcut.getPage() * 12); // C4 Client
		switch(_shortcut.getType())
		{
			case ITEM:
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				writeD(_shortcut.getSharedReuseGroup());
				writeD(0x00); // TODO reuse per second
				writeD(0x00); // ??
				writeH(0x00); // ??
				writeH(0x00); // ??
				writeD(0x00); // ??
				break;
			case SKILL:
				writeD(_shortcut.getId());
				writeD(_shortcut.getLevel());
				writeD(-1); // TODO: Параметр ид для комбо скилов ( -1 обычный скил )
				writeC(0x00); // ??
				writeD(_shortcut.getCharacterType());
				writeD(0x00); // ??
				writeD(0x00); // ??
				break;
			default:
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
		}
	}
}
