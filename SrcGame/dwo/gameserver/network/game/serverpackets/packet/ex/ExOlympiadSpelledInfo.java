package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * @author godson
 */

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private int _playerID;
	private List<Effect> _effects;

	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_effects = new FastList<>();
		_playerID = player.getObjectId();
	}

	public void addEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}

	@Override
	protected void writeImpl()
	{
		writeD(_playerID);
		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(temp._duration / 1000);
		}
	}

	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;

		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}
}
