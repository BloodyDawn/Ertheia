package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 09.09.12
 * Time: 20:08
 * L2GOD Team
 * Хз идей больше нет как правильно эта штуковина должна работать, сейчас она отнимает и прибавляет в зависимости от состояния игрока фиксированное кол-во цп\хп которое указывается в скилле.
 * По необходимости доработать, хотя сейчас вроде работает более менее корректно, т.е. отнимает хп у флагнутых игроков которые не состоят в пати или клане и хиляет союзных игроков коорые состоят в пати или клане\алли.
 */
public class Laksis extends L2Effect
{
	public Laksis(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		L2PcInstance caster = (L2PcInstance) getEffector();

        caster.startAbnormalEffect(AbnormalEffect.S_LAKSIS);
        
		for(L2Character cha : caster.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if(cha == null)
			{
				continue;
			}

			boolean heal = false;

			if(cha instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) getEffector();
				L2PcInstance target = (L2PcInstance) cha;

				if(player.getParty() != null)
				{
					if(player.isInSameParty(target) || player.isInSameChannel(target))
					{
						heal = true;
					}
				}
				if(player.getClan() != null && !player.isInsideZone(L2Character.ZONE_PVP))
				{
					if(player.isInSameClan(target) || player.isInSameAlly(target))
					{
						heal = true;
					}
				}

				if(heal)
				{
					if(target == null || target.isDead())
					{
						return false;
					}

					StatusUpdate su = new StatusUpdate(target);

					double powerCP = calc();
					double powerHP = calc();

					powerCP = Math.min(powerCP, target.getMaxRecoverableCp() - target.getCurrentCp());
					powerHP = Math.min(powerHP, target.getMaxRecoverableHp() - target.getCurrentHp());

					// Prevent negative amounts
					if(powerCP < 0)
					{
						powerCP = 0;
					}
					if(powerHP < 0)
					{
						powerHP = 0;
					}

					// To prevent -value heals, set the value only if current Cp is less than max recoverable.
					if(target.getCurrentCp() < target.getMaxRecoverableCp())
					{
						target.setCurrentCp(powerCP + target.getCurrentCp());
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
						target.sendPacket(sm);
						su.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
						target.sendPacket(su);
					}
					else
					{
						target.setCurrentHp(powerHP + target.getCurrentHp());
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
						target.sendPacket(sm);
						su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
						target.sendPacket(su);
					}
				}
				else
				{
					if(target == null || target.isDead() || target.isInsideZone(L2Character.ZONE_PEACE))
					{
						return false;
					}

					if(target.getPvPFlagController().isFlagged())
					{
						double damage = calc();
						target.reduceCurrentHp(damage, player, getSkill());
					}
				}
			}
		}

		return true;
	}

    @Override
    public void onExit()
    {
        getEffector().stopAbnormalEffect(AbnormalEffect.S_LAKSIS);
    }
}
