package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 15.04.12
 * Time: 17:17
 */

public class _10312_AbandonedGodsCreature extends Quest
{
	// Квестовые персонажи
	private static final int Горфина = 33031;

	// Квестовые предметы
	private static final int КлючАфроса = 17373;

	// Квестовые награды
	private static final int РукоятьКузнецаГигантов = 19305;
	private static final int ЗаготовкаРеоринаГигантов = 19306;
	private static final int НаковальняКузнецаГигантов = 19307;
	private static final int ЗаготовкаОружейникаГигантов = 19308;
	private static final int СвитокR = 17527;
	private static final int МешочекR = 34861;

	// Квестовые монстры
	private static final int Афрос = 25866;

	public _10312_AbandonedGodsCreature()
	{
		addStartNpc(Горфина);
		addTalkId(Горфина);
		addKillId(Афрос);
	}

	public static void main(String[] args)
	{
		new _10312_AbandonedGodsCreature();
	}

	@Override
	public int getQuestId()
	{
		return 10312;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "33031-06.htm":
				st.startQuest();
				break;
			case "33031-10.htm":
				st.addExpAndSp(46847289, 20739487);
				st.giveItem(РукоятьКузнецаГигантов);
				st.giveItem(ЗаготовкаРеоринаГигантов);
				st.giveItem(НаковальняКузнецаГигантов);
				st.giveItem(ЗаготовкаОружейникаГигантов);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "33031-11.htm":
				st.addExpAndSp(46847289, 20739487);
				st.giveItems(СвитокR, 2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "33031-12.htm":
				st.addExpAndSp(46847289, 20739487);
				st.giveItem(МешочекR);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1 && npc.getNpcId() == Афрос)
		{
			if(killer.getParty() == null)
			{
				TIntIntHashMap moblist = new TIntIntHashMap();
				moblist.put(1025775, 1);
				killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
			else
			{
				for(L2PcInstance partyMember : killer.getParty().getMembers())
				{
					if(Util.checkIfInRange(900, killer, partyMember, false))
					{
						st = partyMember.getQuestState(getClass());
						TIntIntHashMap moblist = new TIntIntHashMap();
						moblist.put(1025775, 1);
						if(st != null && st.isStarted())
						{
							partyMember.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.setCond(2);
						}
					}
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState previous = player.getQuestState(_10310_InvertedAxisOfCreation.class);

		if(npc.getNpcId() == Горфина)
		{
			if(previous == null || !previous.isCompleted() || player.getLevel() < 90)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "33031-03.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "33031-02.htm";
				case CREATED:
					return "33031-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33031-07.htm";
					}
					if(st.getCond() == 2)
					{
						return "33031-09.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10310_InvertedAxisOfCreation.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025775, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}
