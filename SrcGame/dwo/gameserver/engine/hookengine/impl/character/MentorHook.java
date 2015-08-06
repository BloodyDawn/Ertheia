package dwo.gameserver.engine.hookengine.impl.character;

import dwo.config.Config;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2Mentee;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExMentorList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MentorHook extends AbstractHookImpl
{
	private static final Logger _log = LogManager.getLogger(MentorHook.class);

	@Override
	public void onLevelIncreased(L2PcInstance player)
	{
		if(player.isMentee() && !player.isSubClassActive())
		{
			try
			{
				// Проверяем уровень игрока и выдаем в соответствии с ним награду учителю
				// Если игрок превысил 85 уровень - автоматически разрываем отношения учитель-ученик
				checkLevelForReward(player);
				if(player.getLevel() >= 85 && !player.getVariablesController().get("menteeDone", Boolean.class, false))
				{
					// Отменяем бафы ученика
					MentorManager.cancelMenteeBuffs(player);
					// Отменяем скиллы учника
					MentorManager.deleteMenteeSkills(player);

					L2Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
					if(mentor != null && mentor.isOnline())
					{
						mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MENTEE_S1_REACHED_LEVEL_86_SO_THE_MENTORING_RELATIONSHIP_WAS_ENDED).addPcName(player));

						if(MentorManager.isAllMenteesOffline(mentor.getObjectId(), player.getObjectId()))
						{
							// Отменяем бафы наставника
							MentorManager.cancelMentorBuffs(mentor.getPlayerInstance());
							// Отменяем скиллы наставника
							MentorManager.deleteMentorSkills(mentor.getPlayerInstance());
						}
					}
					if(mentor != null)
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_REACHED_LEVEL_86_SO_THE_MENTORING_RELATIONSHIP_WITH_YOUR_MENTOR_S1_CAME_TO_AN_END).addString(mentor.getName()));
					}
					player.sendSkillList();

					String title = "Поздравляем Вас с окончанием наставничества";
					String body = "Поздравляем Вас с окончанием наставничества. Обменяйте этот сертификат у ближайшего Помощника Наставника.";
					MailMessage msg = new MailMessage(player.getObjectId(), title, body, "Помощник Наставничества");
					msg.createAttachments();
					msg.getAttachments().addItem(ProcessType.MENTEE, 33800, 1, null, player);
					msg.sendMessage();

					player.getVariablesController().set("menteeDone", true);

					if(mentor != null)
					{
						MentorManager.setPenalty(mentor.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_COMPLETE);
                        MentorManager.getInstance().deleteMentor(mentor.getObjectId(), player.getObjectId());
                    }
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, e.getMessage(), e);
			}
		}
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		MentorManager.getInstance().loadMentees(player.getObjectId());
		MentorManager.getInstance().loadMentor(player.getObjectId());

		if(player.isMentee())
		{
			L2Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
			if(mentor != null && mentor.isOnline())
			{
				// Бафаем наставника
				for(SkillHolder sk : MentorManager.getMenteeEffects(true))
				{
					sk.getSkill().getEffects(mentor.getPlayerInstance(), mentor.getPlayerInstance());
				}

				// Бафаем ученика
				for(SkillHolder sk : MentorManager.getMenteeEffects(false))
				{
					sk.getSkill().getEffects(player, player);
				}

				// Добавляем скилы наставнику
				for(SkillHolder sk : MentorManager.getMenteeSkills(true))
				{
					mentor.getPlayerInstance().addSkill(sk.getSkill());
				}

				// Добавляем скиллы ученику
				for(SkillHolder sk : MentorManager.getMenteeSkills(false))
				{
					player.addSkill(sk.getSkill());
				}

				mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_MENTEE_S1_HAS_CONNECTED).addCharName(player));
				mentor.getPlayerInstance().sendSkillList();
				mentor.sendPacket(new ExMentorList(mentor.getPlayerInstance()));
			}
		}
		else if(player.isMentor())
		{
			// Бафаем ученика
			// Добавляем скиллы ученику
			MentorManager.getInstance().getMentees(player.getObjectId()).stream().filter(mentee -> mentee != null && mentee.isOnline()).forEach(mentee -> {
				// Бафаем ученика
				for(SkillHolder sk : MentorManager.getMenteeEffects(false))
				{
					sk.getSkill().getEffects(mentee.getPlayerInstance(), mentee.getPlayerInstance());
				}

				// Добавляем скиллы ученику
				for(SkillHolder sk : MentorManager.getMenteeSkills(false))
				{
					mentee.getPlayerInstance().addSkill(sk.getSkill());
				}

				mentee.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_MENTOR_S1_HAS_CONNECTED).addCharName(player));
				mentee.getPlayerInstance().sendSkillList();
				mentee.sendPacket(new ExMentorList(mentee.getPlayerInstance()));
			});

			if(!MentorManager.isAllMenteesOffline(player.getObjectId(), 0))
			{
				// Бафаем наставника
				for(SkillHolder sk : MentorManager.getMenteeEffects(true))
				{
					sk.getSkill().getEffects(player, player);
				}

				// Добавляем скиллы наставнику
				for(SkillHolder sk : MentorManager.getMenteeSkills(true))
				{
					player.addSkill(sk.getSkill());
				}

				player.sendSkillList();
			}
		}
	}

	@Override
	public void onDeleteMe(L2PcInstance player)
	{
		if(player.isMentee())
		{
			L2Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
			if(mentor != null && mentor.isOnline())
			{
				if(MentorManager.isAllMenteesOffline(mentor.getObjectId(), player.getObjectId()))
				{
					// Отменяем бафы наставника
					MentorManager.cancelMentorBuffs(mentor.getPlayerInstance());
					// Отменяем скиллы наставника
					MentorManager.deleteMentorSkills(mentor.getPlayerInstance());
				}

				mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_MENTEE_S1_HAS_DISCONNECTED).addCharName(player));
				mentor.getPlayerInstance().sendSkillList();
				mentor.sendPacket(new ExMentorList(mentor.getPlayerInstance()));
			}
		}
		else if(player.isMentor())
		{
			// Отменяем бафы ученика
			// Отменяем скиллы учника
			MentorManager.getInstance().getMentees(player.getObjectId()).stream().filter(mentee -> mentee != null && mentee.isOnline()).forEach(mentee -> {
				// Отменяем бафы ученика
				MentorManager.cancelMenteeBuffs(mentee.getPlayerInstance());
				// Отменяем скиллы учника
				MentorManager.deleteMenteeSkills(mentee.getPlayerInstance());

				mentee.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_MENTOR_S1_HAS_DISCONNECTED).addCharName(player));
				mentee.getPlayerInstance().sendSkillList();
				mentee.sendPacket(new ExMentorList(mentee.getPlayerInstance()));
			});
		}
	}

	/**
	 * Проверяет уровень игрока при успешном завершении обучения
	 * у наставника и в соответсвтии с ним (уровенем) отправляет награду наставнику.
	 * @param player инстанс ученика
	 */
	private void checkLevelForReward(L2PcInstance player)
	{
		if(player.getLevel() < 10 || player.getLevel() > 85)
		{
			return;
		}

		L2Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
		int level = player.getLevel();
		int amount = 0;

		switch(level)
		{
			case 10:
				amount = 2;
				break;
			case 20:
				amount = 30;
				break;
			case 30:
				amount = 54;
				break;
			case 40:
				amount = 131;
				break;
			case 50:
				amount = 210;
				break;
			case 51:
				amount = 215;
				break;
			case 52:
				amount = 239;
				break;
			case 53:
				amount = 265;
				break;
			case 54:
				amount = 292;
				break;
			case 55:
				amount = 319;
				break;
			case 56:
				amount = 348;
				break;
			case 57:
				amount = 378;
				break;
			case 58:
				amount = 409;
				break;
			case 59:
				amount = 440;
				break;
			case 60:
				amount = 474;
				break;
			case 61:
				amount = 509;
				break;
			case 62:
				amount = 518;
				break;
			case 63:
				amount = 553;
				break;
			case 64:
				amount = 534;
				break;
			case 65:
				amount = 568;
				break;
			case 66:
				amount = 586;
				break;
			case 67:
				amount = 619;
				break;
			case 68:
				amount = 653;
				break;
			case 69:
				amount = 688;
				break;
			case 70:
				amount = 722;
				break;
			case 71:
				amount = 673;
				break;
			case 72:
				amount = 707;
				break;
			case 73:
				amount = 742;
				break;
			case 74:
				amount = 776;
				break;
			case 75:
				amount = 811;
				break;
			case 76:
				amount = 827;
				break;
			case 77:
				amount = 586;
				break;
			case 78:
				amount = 617;
				break;
			case 79:
				amount = 650;
				break;
			case 80:
				amount = 691;
				break;
			case 81:
				amount = 871;
				break;
			case 82:
				amount = 911;
				break;
			case 83:
				amount = 952;
				break;
			case 84:
				amount = 995;
				break;
			case 85:
				amount = 1036;
				break;
		}

		if(amount > 0)
		{
			String title = "Монеты Наставничества";
			String body = "Ваш ученик " + player.getName() + " достиг уровня " + level + " и Вы заслужили за это Монеты Наставника. После того, как примите эти монеты и положите в инвентарь - сожгите это письмо. Если Ваш ящик будет переполнен, то к Вам не смогут доставить ни одно письмо.";
			MailMessage msg = new MailMessage(mentor.getObjectId(), title, body, "Гид Наставничества");
			msg.createAttachments();
			msg.getAttachments().addItem(ProcessType.MENTEE, 33804, amount, null, null);
			msg.sendMessage();
		}
	}
}
