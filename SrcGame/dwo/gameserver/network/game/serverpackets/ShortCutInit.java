package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2ShortCut;

import java.util.List;

public class ShortCutInit extends L2GameServerPacket
{
	private List<L2ShortCut> _shortCuts;

	public ShortCutInit(L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return;
		}

		_shortCuts = activeChar.getShortcutController().list();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_shortCuts.size());

		for(L2ShortCut sc : _shortCuts)
		{
			writeD(sc.getType().ordinal());
			writeD(sc.getSlot() + sc.getPage() * 12);

			switch(sc.getType())
			{
				case ITEM:
					writeD(sc.getId());
					writeD(0x01); // ??
					writeD(sc.getSharedReuseGroup());
					writeD(0x00); // ??
					writeD(0x00); // ??
					writeH(0x00); // ??
					writeH(0x00); // ??
					writeD(0x00); // ??
					break;
				case SKILL:
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeD(-1); // TODO: Параметр ид для комбо скилов ( -1 обычный скил )
					writeC(0x00); // ??
					writeD(0x01); // ??
					break;
				default:
					writeD(sc.getId());
					writeD(0x01);
			}
		}
	}
}
