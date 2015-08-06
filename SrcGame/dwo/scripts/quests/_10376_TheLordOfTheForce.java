package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 15.03.12
 * Time: 4:56
 */

public class _10376_TheLordOfTheForce extends Quest
{
	// Квестовые персонажи
	private static final int Zenya = 32140;
	private static final int Keske = 32139;
	private static final int Agnes = 31588;
	private static final int Andrew = 31292;

	// Квестовые монстры
	private static final int Vladivein = 27481;

	// Квестовые предметы
	private static final int BadMagicalPin = 32700;

	public _10376_TheLordOfTheForce()
	{
		addStartNpc(Zenya);
		addTalkId(Zenya, Keske, Agnes, Andrew);
		addKillId(Vladivein);
	}

	public static void main(String[] args)
	{
		new _10376_TheLordOfTheForce();
	}

	@Override
	public int getQuestId()
	{
		return 10376;
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
			case "32140-06.htm":
				st.startQuest();
				break;
			case "32139-03.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "enterInstance":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				// TODO: Вход в инстанс Владивейна, сейчас сразу спаун
				L2Npc mob = addSpawn(Vladivein, player.getLoc(), true);
				((L2Attackable) mob).attackCharacter(player);
				return null;
			case "32139-08.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "teleport_goddard":
				player.teleToLocation(149597, -57249, -2976);
				return null;
			case "31588-03.htm":
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "31292-03.htm":
				st.addExpAndSp(121297500, 48433200);
				st.giveItem(BadMagicalPin);
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
			return super.onKill(npc, killer, isPet);
		}

		if(st.getCond() == 3)
		{
			if(npc.getNpcId() == Vladivein)
			{
				TIntIntHashMap moblist = new TIntIntHashMap();
				moblist.put(1027481, 1);
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Zenya)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "32140-05.htm";
				case CREATED:
					if(player.getLevel() >= 80)
					{
						QuestState prevSt = player.getQuestState(_10375_TheApostlesOfDreams.class);
						if(!player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal() && prevSt != null && prevSt.isCompleted())
						{
							return "32140-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "32140-03.htm";
						}
					}
					else
					{
						return "32140-04.htm";
					}
				case STARTED:
					return "32140-07.htm";
			}
		}
		else if(npc.getNpcId() == Keske)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 1:
						return "32139-02.htm";
					case 2:
					case 3:
						return "32139-03.htm";
					case 4:
						return "32139-04.htm";
					case 5:
						return "32139-08.htm";
				}
			}
		}
		else if(npc.getNpcId() == Agnes)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 5)
				{
					return "31588-01.htm";
				}
				else if(st.getCond() == 6)
				{
					return "31588-03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Andrew)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 6)
				{
					return "31292-01.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState prevSt = player.getQuestState(_10375_TheApostlesOfDreams.class);
		return prevSt != null && prevSt.isCompleted() && player.getLevel() >= 80 && !player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			if(st.getCond() == 3)
			{
				TIntIntHashMap moblist = new TIntIntHashMap();
				moblist.put(1027481, st.getCond() - 2);
				player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			}
		}
	}
}
