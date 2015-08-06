package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

/**
 * L2GOD Team
 * User: Keiichi, Bacek, ANZO, GenCloud
 * Date: 24.05.2011
 * Time: 23:20:46
 */

public class ExAbnormalStatusUpdateFromTarget extends L2GameServerPacket
{
	private int _objectId;
	private FastList<L2Effect> _effects;
	private boolean _isAwakened;

	public ExAbnormalStatusUpdateFromTarget(L2Character object, boolean isAwakened)
	{
		_isAwakened = isAwakened;
		_objectId = object.getObjectId();
		_effects = new FastList<>();
		for(L2Effect effect : object.getAllEffects())
		{
			if(effect != null && effect.getEffectTemplate() != null && effect.getEffectTemplate().icon)
			{
				_effects.add(effect);
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		if(_effects.isEmpty())
		{
			writeH(0x00);
		}
		else
		{
			writeH(_effects.size());
            _effects.stream().filter(e -> e != null).forEach(e -> 
            {
                writeD(e.getSkill().getId()); // ид скила
                writeH(e.getSkill().getLevel());  //лвл скила

                if (_isAwakened)
                {
                    writeH(e.getEffectTemplate().getComboAbnormal());
                } 
                else 
                {
                    writeH(0);
                }

                writeH(e.getSkill().isToggle() || e.getCount() == 2147483647 || e.getEffectTemplate().getTotalTickCount() < 0 ? -1 : Math.max(-1, e.getTimeLeft()));  //время действия в секундах
                writeD(e.getEffector().getObjectId()); // objId того, кто скилл повесил
            });
		}
	}
}
