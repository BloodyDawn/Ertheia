package dwo.gameserver.handler.items;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillTeleport;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

/**
 * Template for item skills handler
 * Only minimum of checks
 */

public class ItemSkillsTemplate implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		L2PcInstance activeChar = playable.getActingPlayer();
		if(!playable.isPet() && !playable.isPlayer())
		{
			return false;
		}

		if(!EventManager.onScrollUse(activeChar))
		{
			playable.sendActionFailed();
			return false;
		}

		// Pets can use items only when they are tradable.
		if(playable.isPet() && !item.isTradeable())
		{
			activeChar.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}

		if(!checkReuse(activeChar, null, item))
		{
			return false;
		}

		int skillId;
		int skillLvl;

		SkillHolder[] skills = item.getEtcItem().getSkills();
		if(skills == null)
		{
			_log.log(Level.WARN, "Item " + item + " does not have registered any skill for handler.");
			return false;
		}

		for(SkillHolder skillInfo : skills)
		{
			if(skillInfo == null)
			{
				continue;
			}

			skillId = skillInfo.getSkillId();
			skillLvl = skillInfo.getSkillLvl();
			L2Skill itemSkill = skillInfo.getSkill();

			if(itemSkill != null)
			{
				if(!itemSkill.checkCondition(playable, playable.getTarget(), false))
				{
					return false;
				}

				if(playable.isSkillDisabled(itemSkill))
				{
					return false;
				}

				// Verify that skill is not under reuse.
				if(!checkReuse(activeChar, itemSkill, item))
				{
					return false;
				}

				if(!itemSkill.isStatic() && playable.isCastingNow())
				{
					return false;
				}

				if(itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsumeCount() > 0 && (itemSkill.isStatic() || itemSkill.isSimultaneousCast()))
				{
					if(!playable.destroyItem(ProcessType.CONSUME, item.getObjectId(), itemSkill.getItemConsumeCount(), playable, false))
					{
						activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						return false;
					}
				}

				if(playable instanceof L2PcInstance && itemSkill.isVitalityItemSkill())
				{
					if(((L2PcInstance) playable).getVitalityDataForCurrentClassIndex().getVitalityItems() > 0)
					{
						((L2PcInstance) playable).decreaseVitalityItemsLeft();
						((L2PcInstance) playable).broadcastUserInfo();
					}
					else
					{
						playable.sendMessage("Лимит достпуных для использования предметов жизненной энергии исчерпан."); // TODO: сообщение из клиента
						return false;
					}
				}

				// send message to owner
				if(playable.isPet())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
					sm.addString(itemSkill.getName());
					activeChar.sendPacket(sm);
				}
				else
				{
					switch(skillId)
					{
						// short buff icon for healing potions
						case 2031:
						case 2032:
						case 2037:
						case 26025:
						case 26026:
							int buffId = activeChar._shortBuffTaskSkillId;
							// greater healing potions
							if(skillId == 2037 || skillId == 26025 || (skillId == 2032 || skillId == 26026) && buffId != 2037 && buffId != 26025)
							{
								activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getBuffDuration() / 1000);
							}
							else
							{
								if(buffId != 2037 && buffId != 26025 && buffId != 2032 && buffId != 26026)
								{
									activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getBuffDuration() / 1000);
								}
							}
							break;
					}
				}

				if(itemSkill.isStatic() || itemSkill.isSimultaneousCast())
				{
					// ВНИМАНИЕ: Скилы сое являются isStatic() - по-этому и вынесено из нижеследующего условия как частный случай
					if(itemSkill instanceof L2SkillTeleport)
					{
						if(!playable.useMagic(itemSkill, forceUse, false))
						{
							return false;
						}
					}
					else
					{

						playable.doSimultaneousCast(itemSkill);

						// Summons should be affected by herbs too, self time effect is handled at L2Effect constructor
						if(!playable.isPet() && item.getItemType() == L2EtcItemType.HERB && !activeChar.getPets().isEmpty())
						{
							activeChar.getPets().stream().filter(summon -> summon instanceof L2SummonInstance).forEach(summon -> summon.doSimultaneousCast(itemSkill));
						}
					}
				}
				else
				{
					playable.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

					if(!playable.useMagic(itemSkill, forceUse, false))
					{
						return false;
					}

					// Consume.
					if(itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsumeCount() > 0)
					{
						// Для extractable-предметов, распакову которых можно отменить
						if(itemSkill.getHitTime() > 500 && itemSkill.getHitTime() < 20000)
						{
							ThreadPoolManager.getInstance().scheduleEffect(() -> {
								if(!playable.isCastingNow() || ((L2PcInstance) playable).getCurrentSkill().getSkillId() != itemSkill.getId())
								{
									return;
								}

								if(!playable.destroyItem(ProcessType.CONSUME, item.getObjectId(), itemSkill.getItemConsumeCount(), null, false))
								{
									activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
									activeChar.abortCast();
								}
							}, itemSkill.getHitTime());
						}
						else if(!playable.destroyItem(ProcessType.CONSUME, item.getObjectId(), itemSkill.getItemConsumeCount(), null, false))
						{
							activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
							return false;
						}
					}
				}

				if(itemSkill.getReuseDelay() > 0)
				{
					activeChar.addTimeStamp(itemSkill, itemSkill.getReuseDelay());
				}
			}
		}
		return true;
	}

	/**
	 * @param player the player using the item or skill
	 * @param skill the skill being used, can be null
	 * @param item the item being used
	 * @return {@code true} if the the item or skill to check is available, {@code false} otherwise
	 */
	private boolean checkReuse(L2PcInstance player, L2Skill skill, L2ItemInstance item)
	{
		SystemMessage sm = null;
		long remainingTime = skill != null ? player.getSkillRemainingReuseTime(skill.getReuseHashCode()) : player.getItemRemainingReuseTime(item.getObjectId());
		boolean isAvailable = remainingTime <= 0;
		if(!isAvailable)
		{
			int hours = (int) (remainingTime / 3600000L);
			int minutes = (int) (remainingTime % 3600000L) / 60000;
			int seconds = (int) (remainingTime / 1000 % 60);
			if(hours > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
				if(skill == null || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
				sm.addNumber(hours);
				sm.addNumber(minutes);
			}
			else if(minutes > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
				if(skill == null || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
				sm.addNumber(minutes);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
				if(skill == null || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
			}
			sm.addNumber(seconds);
		}
		else if(skill == null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addItemName(item);
		}
		if(sm != null)
		{
			player.sendPacket(sm);
		}
		return isAvailable;
	}
}