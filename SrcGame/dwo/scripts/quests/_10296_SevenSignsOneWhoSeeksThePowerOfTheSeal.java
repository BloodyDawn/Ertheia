package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;

public class _10296_SevenSignsOneWhoSeeksThePowerOfTheSeal extends Quest
{
	// NPC
	private static final int ErisEvilThoughts = 32792;
	private static final int Gruff_looking_Man = 32862;
	private static final int Elcadia_Support = 32787;
	private static final int Elcadia = 32784;
	private static final int Hardin = 30832;
	private static final int Wood = 32593;
	private static final int Franz = 32597;
	private static final int Odd_Globe = 32815;
	// Mobs
	private static final int EtisVanEtin = 18949;
	// Item
	private static final int CertificateOfDawn = 17265;

	public _10296_SevenSignsOneWhoSeeksThePowerOfTheSeal()
	{
		addStartNpc(ErisEvilThoughts);
		addTalkId(ErisEvilThoughts);
		addTalkId(Gruff_looking_Man);
		addTalkId(Elcadia);
		addKillId(EtisVanEtin);
		addTalkId(Hardin);
		addTalkId(Wood);
		addTalkId(Franz);
		addStartNpc(Odd_Globe);
		addTalkId(Odd_Globe);
		addTalkId(Elcadia_Support);
	}

	public static void main(String[] args)
	{
		new _10296_SevenSignsOneWhoSeeksThePowerOfTheSeal();
	}

	@Override
	public int getQuestId()
	{
		return 10296;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(event.equalsIgnoreCase("32792-04.htm"))
			{
				st.startQuest();
				return null;
			}
		}
		else if(npc.getNpcId() == Elcadia)
		{
			if(event.equalsIgnoreCase("32784-03.html"))
			{
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return null;
			}
		}
		else if(npc.getNpcId() == Hardin)
		{
			if(event.equalsIgnoreCase("see"))
			{
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "30832-03.html";
			}
		}
		else if(npc.getNpcId() == Franz)
		{
			if(event.equalsIgnoreCase("reward"))
			{
				if(player.isSubClassActive())
				{
					return "32597-04.html";
				}
				else
				{
					st.addExpAndSp(125000000, 12500000);
					st.giveItems(CertificateOfDawn, 1);
					st.unset("boss");
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "32597-03.html";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		if(npc.getNpcId() == EtisVanEtin)
		{
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_BOSS_CLOSING);
			if(st.getInt("boss") != 1)
			{
				st.set("boss", "1");
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(st.isCompleted())
			{
				return "32792-02.html";
			}
			else if(player.getLevel() < 81)
			{
				return "32792-12.html";
			}
			else if(player.getQuestState(_10295_SevenSignsSolinasTomb.class) == null || !player.getQuestState(_10295_SevenSignsSolinasTomb.class).isCompleted())
			{
				return "32792-12.html";
			}
			else if(st.isCreated())
			{
				return "32792-01.htm";
			}
			else if(st.getCond() == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "32792-05.html";
			}
			else if(st.getCond() == 2)
			{
				return "32792-06.html";
			}
		}
		else if(npc.getNpcId() == Elcadia)
		{
			if(st.getCond() == 3)
			{
				return "32784-01.html";
			}
			else if(st.getCond() == 4)
			{
				return "32784-04.html";
			}
		}
		else if(npc.getNpcId() == Hardin)
		{
			if(st.getCond() == 4)
			{
				return "30832-01.html";
			}
			if(st.getCond() == 5)
			{
				return "30832-03.html";
			}
		}
		else if(npc.getNpcId() == Wood)
		{
			if(st.getCond() == 5)
			{
				return "32593-01.html";
			}
		}
		else if(npc.getNpcId() == Franz)
		{
			if(st.getCond() == 5)
			{
				return "32597-01.html";
			}
		}
		else if(npc.getNpcId() == Odd_Globe)
		{
			return "32815-01.html";
		}
		else if(npc.getNpcId() == Elcadia_Support)
		{
			if(st.getCond() == 2 && st.getInt("boss") == 1)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "32787-01.html";
			}
			else if(st.getCond() == 3)
			{
				return "32787-01.html";
			}
		}
		return null;
	}
}
