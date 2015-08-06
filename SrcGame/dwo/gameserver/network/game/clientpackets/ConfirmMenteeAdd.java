package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExMentorList;
import dwo.gameserver.util.database.DatabaseUtils;
import dwo.scripts.npc.town.MentorGuide;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.11
 * Time: 18:19
 */

public class ConfirmMenteeAdd extends L2GameClientPacket
{
	private int _confirmed;
	private String _mentor;

	/**
	 * @param mentor наставник
	 * @param mentee ученик
	 * @return {@code true} если наставник и ученик удоволетворяют условиям
	 */
	public static boolean validate(L2PcInstance mentor, L2PcInstance mentee)
	{
		if(mentor == null || mentee == null)
		{
			return false;
		}
		if(!mentee.isOnline())
		{
			mentor.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return false;
		}
		if(mentee.isSubClassActive())
		{
			mentor.sendPacket(SystemMessageId.getSystemMessageId(3710)); // TODO: SystemMessageId
			return false;
		}
		if(!mentor.isAwakened() || mentor.getLevel() < 85)
		{
			mentor.sendPacket(SystemMessageId.YOU_MUST_AWAKEN_IN_ORDER_TO_BECOME_A_MENTOR);
			return false;
		}
		if(MentorManager.getMentorPenalty(mentor.getObjectId()) > System.currentTimeMillis())
		{
			long remainingTime = (MentorManager.getMentorPenalty(mentor.getObjectId()) - System.currentTimeMillis()) / 1000;
			int days = (int) (remainingTime / 86400);
			remainingTime %= 86400;
			int hours = (int) (remainingTime / 3600);
			remainingTime %= 3600;
			int minutes = (int) (remainingTime / 60);
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_BOND_WITH_A_NEW_MENTEE_IN_S1_DAYS_S2_HOURS_S3_MINUTES);
			msg.addNumber(days);
			msg.addNumber(hours);
			msg.addNumber(minutes);
			mentor.sendPacket(msg);
			return false;
		}
		if(mentor.getObjectId() == mentee.getObjectId())
		{
			mentor.sendPacket(SystemMessageId.YOU_CANNOT_BECOME_YOUR_OWN_MENTEE);
			return false;
		}
		if(mentee.getLevel() >= 86)
		{
			mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ABOVE_LEVEL_86_AND_CANNOT_BECOME_A_MENTEE).addCharName(mentee));
			return false;
		}
		if(MentorManager.getInstance().getMentees(mentor.getObjectId()) != null && MentorManager.getInstance().getMentees(mentor.getObjectId()).size() >= 3)
		{
			mentor.sendPacket(SystemMessageId.A_MENTOR_CAN_HAVE_UP_TO_3_MENTEES_AT_THE_SAME_TIME);
			return false;
		}
		if(mentee.isMentee())
		{
			mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_HAS_A_MENTOR).addCharName(mentee));
			return false;
		}
		return true;
	}

	@Override
	protected void readImpl()
	{
		_confirmed = readD();
		_mentor = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance mentee = getClient().getActiveChar();
		if(mentee == null)
		{
			return;
		}

		L2PcInstance mentor = WorldManager.getInstance().getPlayer(_mentor);
		if(mentor == null)
		{
			return;
		}

		if(_confirmed == 0)
		{
			mentee.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_DECLINED_S1_MENTORING_OFFER).addCharName(mentor));
			mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DECLINED_BECOMING_YOUR_MENTEE).addCharName(mentee));
		}
		else
		{
			if(validate(mentor, mentee))
			{
				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO character_mentees (charId, mentorId) VALUES (?, ?)");
					statement.setInt(1, mentee.getObjectId());
					statement.setInt(2, mentor.getObjectId());
					statement.execute();

					MentorManager.getInstance().addMentor(mentor.getObjectId(), mentee.getObjectId());

					mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FROM_NOW_ON_S1_WILL_BE_YOUR_MENTEE).addCharName(mentee));
					mentor.sendPacket(new ExMentorList(mentor));

					mentee.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FROM_NOW_ON_S1_WILL_BE_YOUR_MENTOR).addCharName(mentor));
					mentee.sendPacket(new ExMentorList(mentee));

					// Шлем ученику уши один раз при первой попытке завершить наставничество
					if(!mentee.getVariablesController().get("menteeIsEarSended", Boolean.class, false))
					{
						String title = "Поздравляем Вас со вступлением в ученикию";
						String body = "Я очень рад, что теперь дорогу к бесчисленным приключениям Вам покажет сильный наставник.<br>" + "После окончания обучения (достижения 85-го уровня) отнесите мне Сертификат Подопечного, и я обменяю его на экипировку ранга R и Сертификат Окончания.<br>" + "Веселого Вам времяпрепровождения в Lineage 2 с наставником!";
						MailMessage msg = new MailMessage(mentee.getObjectId(), title, body, "Помощник Наставничества");
						msg.createAttachments();
						msg.getAttachments().addItem(ProcessType.MENTEE, 34759, 1, null, mentee);
						msg.sendMessage();

						mentee.getVariablesController().set("menteeIsEarSended", true);
					}

					Quest quest = QuestManager.getInstance().getQuest(MentorGuide.class);
					if(quest != null)
					{
						quest.onEnterWorld(mentor);
					}
					else
					{
						_log.log(Level.ERROR, "MentorGuide quest is missing cannot apply mentor buffs!");
					}
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, e.getMessage(), e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:BC ConfirmMenteeAdd";
	}
}
