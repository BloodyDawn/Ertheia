package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2Mentee;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.11
 * Time: 18:22
 */

public class RequestMentorCancel extends L2GameClientPacket
{
	private int _confirmed;
	private String _name;

	@Override
	protected void readImpl()
	{
		_confirmed = readD();
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		if(_confirmed != 1)
		{
			return;
		}

		L2PcInstance player = getClient().getActiveChar();
		int objectId = CharNameTable.getInstance().getIdByName(_name);
		if(player != null)
		{
			if(player.isMentor())
			{
				L2Mentee mentee = MentorManager.getInstance().getMentee(player.getObjectId(), objectId);
				if(mentee != null)
				{
					// Отменяем бафы ученика
					MentorManager.cancelMenteeBuffs(mentee.getPlayerInstance());
					// Отменяем скиллы учника
					MentorManager.deleteMenteeSkills(mentee.getPlayerInstance());

					if(MentorManager.isAllMenteesOffline(player.getObjectId(), mentee.getObjectId()))
					{
						// Отменяем бафы наставника
						MentorManager.cancelMentorBuffs(player);
						// Отменяем скиллы наставника
						MentorManager.deleteMentorSkills(player);
					}

					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED).addString(_name));
					MentorManager.setPenalty(player.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(player.getObjectId(), mentee.getObjectId());
				}

			}
			else if(player.isMentee())
			{
				L2Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
				if(mentor != null && mentor.getObjectId() == objectId)
				{
					// Отменяем бафы ученика
					MentorManager.cancelMenteeBuffs(player);
					// Отменяем скиллы учника
					MentorManager.deleteMenteeSkills(player);

					if(MentorManager.isAllMenteesOffline(mentor.getObjectId(), player.getObjectId()))
					{
						// Отменяем бафы наставника
						MentorManager.cancelMentorBuffs(mentor.getPlayerInstance());
						// Отменяем скиллы наставника
						MentorManager.deleteMentorSkills(mentor.getPlayerInstance());
					}

					MentorManager.setPenalty(mentor.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(mentor.getObjectId(), player.getObjectId());
					mentor.getPlayerInstance().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED).addString(_name));
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:DB RequestMentorCancel";
	}
}