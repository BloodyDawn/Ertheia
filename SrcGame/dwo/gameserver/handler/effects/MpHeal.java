/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class MpHeal extends L2Effect
{
	public MpHeal(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANA_HEAL;
	}

	@Override
	public boolean onStart()
	{
		double mp = getEffected().getCurrentMp();
		double maxmp = getEffected().getMaxMp();
		double effmp = calc();
		if(mp + effmp >= maxmp)
		{
			effmp = maxmp - mp;
		}
		mp += effmp;
		getEffected().setCurrentMp(mp);
		StatusUpdate sump = new StatusUpdate(getEffected());
		sump.addAttribute(StatusUpdate.CUR_MP, (int) mp);
		getEffected().sendPacket(sump);
		SystemMessage smp = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
		smp.addNumber((int) effmp);
		getEffected().sendPacket(smp);
		return true;
	}

}
