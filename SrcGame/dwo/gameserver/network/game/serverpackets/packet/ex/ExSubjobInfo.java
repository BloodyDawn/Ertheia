package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/*
 * @author Keiichi, ANZO
 * Классификация сабов: 0 - основной класс, 1 - дуалкласс, 2 - обычный саб
 */

public class ExSubjobInfo extends L2GameServerPacket
{
	private L2PcInstance player;

	public ExSubjobInfo(L2PcInstance _cha)
	{
		player = _cha;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x00);
		writeD(player.getClassId().getId());
		writeD(player.getRace().ordinal());

		writeD(player.getSubClasses().size() + 1); // кол-во подклассов+основной класс.

		// Основной класс
		writeD(player.getClassIndex());
		writeD(player.getBaseClassId());
		writeD(player.getStat().getLevelBaseClass());
		writeC(0x00);

		// Все остальные подклассы
		for(SubClass tmp : player.getSubClasses().values())
		{
			writeD(tmp.getClassIndex());
			writeD(tmp.getClassId());
			writeD(tmp.getLevel());
			writeC(tmp.getClassType().ordinal());
		}
	}
}
