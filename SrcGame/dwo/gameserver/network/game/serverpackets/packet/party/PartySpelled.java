package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

public class PartySpelled extends L2GameServerPacket
{
	private List<Effect> _effects;
	private L2Character _activeChar;

	public PartySpelled(L2Character cha)
	{
		_effects = new FastList<>();
		_activeChar = cha;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar instanceof L2SummonInstance ? 2 : _activeChar instanceof L2PetInstance ? 1 : 0);
		writeD(_activeChar.getObjectId());
		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._dat);
			writeD(temp._duration / 1000);
		}

	}

	public void addPartySpelledEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}

	private static class Effect
	{
		protected int _skillId;
		protected int _dat;
		protected int _duration;

		public Effect(int pSkillId, int pDat, int pDuration)
		{
			_skillId = pSkillId;
			_dat = pDat;
			_duration = pDuration;
		}
	}
}
