package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.scripts.instances.NQ_MuseumDungeon;

public class _10327_IntruderWhoWantsTheBookOfGiants extends Quest
{
	// Quest Npc
	private static final int PANTHEON = 32972;
	private static final int TOYRON = 33004;
	private static final int BOOKS = 33126;

	// Quest Item
	private static final int BOOK = 17575;

	// Quest Mobs
	private static final int THIEF = 23121;

	public _10327_IntruderWhoWantsTheBookOfGiants()
	{
		addStartNpc(PANTHEON);
		addFirstTalkId(TOYRON);
		addFirstTalkId(BOOKS);
		addTalkId(PANTHEON, TOYRON, BOOKS);
		questItemIds = new int[]{BOOK};
	}

	public static void main(String[] args)
	{
		new _10327_IntruderWhoWantsTheBookOfGiants();
	}

	@Override
	public int getQuestId()
	{
		return 10327;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && st.getPlayer().getLevel() < 20 && !st.isCompleted())
		{
			st.startQuest();
			return "si_illusion_pantheon_q10327_05.htm";
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int cond = st.getCond();

		if(npc.getNpcId() == PANTHEON)
		{
			if(reply == 1)
			{
				return "si_illusion_pantheon_q10327_04.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				NQ_MuseumDungeon.getInstance().enterInstance(player);
			}
			else if(reply == 3 && cond == 3)
			{
				st.giveAdena(16000, true);
				st.giveItems(112, 1);    // Apprentice's Earring
				st.giveItems(112, 1);    // Apprentice's Earring
				st.takeItems(BOOK, -1);
				st.addExpAndSp(7800, 5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(11022201), ExShowScreenMessage.MIDDLE_CENTER, 4000));
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_pantheon_q10327_08.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState previous = player.getQuestState(_10326_RespectToTheOldMan.class);

		int cond = st.getCond();
		if(npc.getNpcId() == PANTHEON)
		{
			switch(st.getState())
			{
				case CREATED:
					if(previous != null && previous.isCompleted() && player.getLevel() < 20)
					{
						return "si_illusion_pantheon_q10327_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_pantheon_q10327_02.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "si_illusion_pantheon_q10327_06.htm";
					}
					if(cond == 2)
					{
						return "si_illusion_pantheon_q10327_06a.htm";
					}
					if(cond == 3)
					{
						return "si_illusion_pantheon_q10327_07.htm";
					}
					break;
				case COMPLETED:
					return "si_illusion_pantheon_q10327_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return "si_illusion_guard1001.htm";
		}

		if(npc.getNpcId() == TOYRON)
		{
			if(player.getInventory().getItemsByItemId(BOOK) == null || st.getCond() == 1)
			{
				return "si_illusion_guard1_q10327_01.htm";
			}
			else if(st.getCond() == 2)
			{
				return "si_illusion_guard1_q10327_02.htm";
			}
			else
			{
				return st.getCond() == 3 ? "si_illusion_guard1001.htm" : "si_illusion_guard1001.htm";
			}
		}
		if(npc.getNpcId() == BOOKS)
		{
			InstanceManager.InstanceWorld currentWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(currentWorld instanceof NQ_MuseumDungeon.MuseumDangeon)
			{
				NQ_MuseumDungeon.MuseumDangeon museumWorld = (NQ_MuseumDungeon.MuseumDangeon) currentWorld;

				if(museumWorld._book == null)
				{
					double rand = Math.random();
					if(rand <= 0.25)
					{
						museumWorld._book = museumWorld._books.get(0);   // TODO нпе
					}
					else if(rand > 0.25 && rand <= 0.5)
					{
						museumWorld._book = museumWorld._books.get(1);   // TODO нпе
					}
					else if(rand > 0.5 && rand <= 0.75)
					{
						museumWorld._book = museumWorld._books.get(2);   // TODO нпе
					}
					else if(rand > 0.75)
					{
						museumWorld._book = museumWorld._books.get(3);   // TODO нпе    IndexOutOfBoundsException: index: 3
					}
				}

				if(museumWorld._book != null && npc.getObjectId() == museumWorld._book.getObjectId() && st.getCond() == 1)
				{
					st.setCond(2);
					st.giveItems(BOOK, 1);
					// Thieves
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1811194), ExShowScreenMessage.MIDDLE_CENTER, 4000));
					Quest dungeon = QuestManager.getInstance().getQuest(NQ_MuseumDungeon.class);
					if(dungeon != null)
					{
						((NQ_MuseumDungeon) dungeon).spawnNpc(player);
						((NQ_MuseumDungeon) dungeon).startToyronFollow(player);
					}
					return "book_success_q10327_01.htm";
				}
				else if(st.getCond() == 1)
				{
					return "book_fail_q10327_01.htm";
				}
				else if(st.getCond() > 1)
				{
					return "desk_q10327_01.htm";
				}
			}
		}

		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10326_RespectToTheOldMan.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;
	}
} 