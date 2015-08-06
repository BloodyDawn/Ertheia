package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.CharEffectList;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
    private int _mask;

	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
        _mask = _activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode() ? 1 : 0;
        _mask |= _activeChar.isInsideZone(L2Character.ZONE_DANGERAREA) ? 2 : 0;
        _mask |= _activeChar.isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE) ? 4 : 0;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_activeChar.getCharges()); // 1-15 increase force, lvl
		writeD(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeC(_activeChar.getExpertiseWeaponPenalty()); // Weapon Grade Penalty [1-4]
		writeC(_activeChar.getExpertiseArmorPenalty()); // Armor Grade Penalty [1-4]
		writeC(0x00); // 1-15 death penalty, lvl (combat ability decreased due to death)
		writeC(_activeChar.getSouls());
		writeC(_mask);
	}
}
