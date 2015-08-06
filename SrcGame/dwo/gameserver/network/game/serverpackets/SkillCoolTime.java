package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.TimeStamp;
import javolution.util.FastList;

import java.util.stream.Collectors;

public class SkillCoolTime extends L2GameServerPacket
{
	private final FastList<TimeStamp> _skillReuseTimeStamps = new FastList<>();

	public SkillCoolTime(L2PcInstance cha)
	{
		_skillReuseTimeStamps.addAll(cha.getSkillReuseTimeStamps().values().stream().filter(TimeStamp::hasNotPassed).collect(Collectors.toList()));
	}

	@Override
	protected void writeImpl()
	{
		writeD(_skillReuseTimeStamps.size()); // list size
		for(TimeStamp ts : _skillReuseTimeStamps)
		{
			writeD(ts.getSkillId());
			writeD(0x00);
			writeD((int) ts.getReuse() / 1000);
			writeD((int) ts.getRemaining() / 1000);
		}
	}
}
