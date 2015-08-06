package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExMagicAttackInfo extends L2GameServerPacket
{
	public static final int OVER_HIT = 3;
	public static final int ATTACK_WAS_BLOCKED = 8;
	private final int _caster;

	// 1 крит    красным
	// 2 крит	 зеленым
	// 3 сверхудар
	// 4 уклонение красным
	// 5 блокирование красным
	// 6 сопротивление жолтым
	// 7 имунитет красным
	// 8 имунитет красным
	private final int _target;
	private final int _effectId;

	public ExMagicAttackInfo(int caster, int target, int effectId)
	{
		_caster = caster;
		_target = target;
		_effectId = effectId;
	}

	public ExMagicAttackInfo(L2Character caster, L2Character target, int effectId)
	{
		_caster = caster.getObjectId();
		_target = target.getObjectId();
		_effectId = effectId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_caster);    // CasterId
		writeD(_target);    // TargetId
		writeD(_effectId);    // Unk
	}
}