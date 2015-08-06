package dwo.gameserver.network.game.serverpackets.packet.pet;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PetStatusUpdate extends L2GameServerPacket
{
	private L2Summon _summon;
	private int _maxHp;
	private int _maxMp;
	private int _maxFed;
	private int _curFed;

	public PetStatusUpdate(L2Summon summon)
	{
		_summon = summon;
		_maxHp = _summon.getMaxVisibleHp();
		_maxMp = _summon.getMaxMp();
		if(_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if(_summon instanceof L2SummonInstance)
		{
			L2SummonInstance sum = (L2SummonInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeS("");
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxVisibleHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel());// 0% absolute value
		writeQ(_summon.getExpForNextLevel());// 100% absolute value
		writeD(0x00); // TODO: GOD
	}
}
