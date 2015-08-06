package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * L2GOD Team
 * User: Keiichi, ANZO
 * Date: 02.06.12
 * Time: 16:50
 */

public class Plunder extends L2Effect
{
	public Plunder(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PLUNDER;
	}

	@Override
	public boolean onStart()
	{

		if(!(getEffector() instanceof L2PcInstance))
		{
			return false;
		}

		if(!(getEffected() instanceof L2MonsterInstance))
		{
			return false;
		}

		L2MonsterInstance target = (L2MonsterInstance) getEffected();
		L2PcInstance player = (L2PcInstance) getEffector();

		if(target == null)
		{
			return false;
		}

		if(target.isSpoil())
		{
			player.sendPacket(SystemMessageId.ALREADY_SPOILED);
			return false;
		}

		boolean spoil = false;
		ItemHolder[] items = null;
		if(!target.isDead())
		{
			spoil = MagicalDamage.calcMagicSuccess(player, target, getSkill());

			if(spoil)
			{
				target.setSpoil(true);
				target.setIsSpoiledBy(player);
				target.doSpoil(player);

				if(target.isSweepActive())
				{
					items = target.takeSweep();
					if(items == null || items.length == 0)
					{
						return false;
					}
					for(ItemHolder ritem : items)
					{
						if(player.isInParty())
						{
							player.getParty().distributeItem(player, ritem, true, target);
						}
						else
						{
							player.addItem(ProcessType.PLUNDER, ritem.getId(), ritem.getCount(), player, true);
						}
					}
					player.sendPacket(SystemMessageId.SPOIL_SUCCESS);
				}
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(getSkill()));
			}
		}
		return true;
	}

}