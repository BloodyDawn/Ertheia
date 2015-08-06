package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExVoteSystemInfo;

public class NewVoteSociality extends L2GameClientPacket
{
	private int _targetId;

	@Override
	protected void readImpl()
	{
		_targetId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Object object = activeChar.getTarget();

		if(!(object instanceof L2PcInstance))
		{
			if(object == null)
			{
				activeChar.sendPacket(SystemMessageId.SELECT_TARGET);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			}
			return;
		}

		L2PcInstance target = (L2PcInstance) object;

		if(target.getObjectId() != _targetId)
		{
			return;
		}

		if(target.equals(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}

		if(activeChar.getRecommendationsLeft() <= 0)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CURRENTLY_DO_NOT_HAVE_ANY_RECOMMENDATIONS);
			return;
		}

		if(target.getRecommendations() >= 255)
		{
			activeChar.sendPacket(SystemMessageId.YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}

		activeChar.giveRecommendation(target);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addPcName(target);
		sm.addNumber(activeChar.getRecommendationsLeft());
		activeChar.sendPacket(sm);

		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addPcName(activeChar);
		target.sendPacket(sm);

		activeChar.sendUserInfo();
		target.broadcastUserInfo();

		// Шлем инфу о рекоммендациях обоим (зачем - хз, так на офе)
		activeChar.sendPacket(new ExVoteSystemInfo(activeChar));
		target.sendPacket(new ExVoteSystemInfo(target));
	}

	@Override
	public String getType()
	{
		return "[C] D0:7B NewVoteSystem";
	}
}
