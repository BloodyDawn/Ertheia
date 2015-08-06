package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

public class RequestCallToChangeClass extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Триггер
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isSubClassActive() && !activeChar.getSubclass().isDualClass() || activeChar.getOlympiadController().isParticipating() || activeChar.getInstanceId() > 0)
		{
			return;
		}

		// TODO: После доработки инстанса гномов включить
		/*QuestState qs = activeChar.getQuestState("AW_SorrowfulRemembrance");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("enterInstance",null,activeChar);
		}*/
		activeChar.teleToLocation(-114710, 243831, -7968);
	}

	@Override
	public String getType()
	{
		return "[C] D0:A4 RequestCallToChangeClass";
	}
}
