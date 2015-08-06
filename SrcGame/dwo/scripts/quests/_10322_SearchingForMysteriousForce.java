package dwo.scripts.quests;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.08.11
 * Time: 14:15
 */

public class _10322_SearchingForMysteriousForce extends Quest
{
	// Квестовые персонажи
	private static final int SHANNON = 32974;
	private static final int YIBEIN = 33464;
	private static final int NEWBIE_HELPER = 32981;
	private static final int SCARESCROW = 27457;
	private static final int ARENA_WALKER = 33016;

	// Бафы путешественника
	private static final int[] BUFFS_FIGHTER = {5627, 5628, 5637, 5629, 5630, 5631};
	private static final int[] BUFFS_MAGE = {5627, 5628, 5637, 5634, 5635, 5636};

	public _10322_SearchingForMysteriousForce()
	{
		addStartNpc(SHANNON);
		addTalkId(SHANNON, YIBEIN, NEWBIE_HELPER);
		addKillId(SCARESCROW);
	}

	public static void main(String[] args)
	{
		new _10322_SearchingForMysteriousForce();
	}

	@Override
	public int getQuestId()
	{
		return 10322;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			L2Npc arenaWalker = addSpawn(ARENA_WALKER, SpawnTable.getInstance().getFirstSpawn(SHANNON).getLastSpawn());
			arenaWalker.broadcastPacket(new NS(arenaWalker.getObjectId(), ChatType.NPC_ALL, arenaWalker.getNpcId(), 1032302).addStringParameter(qs.getPlayer().getName()));
			arenaWalker.setTarget(qs.getPlayer());
			WalkingManager.getInstance().startMoving(arenaWalker, 12);
			return "si_illusion_shannon_q10322_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == SHANNON)
		{
			if(reply == 1)
			{
				return "si_illusion_shannon_q10322_04.htm";
			}
		}
		else if(npc.getNpcId() == NEWBIE_HELPER)
		{
			if(reply == 1)
			{
				st.setCond(5);
				npc.setTarget(player);
				if(player.isMageClass())
				{
					for(int buffId : BUFFS_MAGE)
					{
						npc.doCast(SkillTable.getInstance().getInfo(buffId, 1));
					}
				}
				else
				{
					for(int buffId : BUFFS_FIGHTER)
					{
						npc.doCast(SkillTable.getInstance().getInfo(buffId, 1));
					}
				}
				player.sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, "..\\L2text\\QT_002_Guide_01.htm"));
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "si_newbie_guide_new_q10322_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == SCARESCROW)
		{
			if(st.getCond() == 2)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(st.getCond() == 5)
			{
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == SHANNON)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "si_illusion_shannon_q10322_02.htm";
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_shannon_q10322_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_shannon_q10322_03.htm";
					}
				case STARTED:
					return "si_illusion_shannon_q10322_06.htm";
			}
		}
		else if(npc.getNpcId() == YIBEIN)
		{
			switch(st.getCond())
			{
				case 1:
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "si_illusion_evein_q10322_01.htm";
				case 2:
					return "si_illusion_evein_q10322_02.htm";
				case 3:
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "si_illusion_evein_q10322_03.htm";
				case 4:
					return "si_illusion_evein_q10322_04.htm";
				case 5:
					return "si_illusion_evein_q10322_04a.htm";
				case 6:
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1032201), ExShowScreenMessage.MIDDLE_CENTER, 4000));
					st.addExpAndSp(300, 5);
					st.giveAdena(7000, true);
					st.giveItems(7816, 1); // Apprentice Adventurer's Staff
					st.giveItems(7817, 1); // Apprentice Adventurer's Club
					st.giveItems(7818, 1); // Apprentice Adventurer's Knife
					st.giveItems(7819, 1); // Apprentice Adventurer's cestus
					st.giveItems(7820, 1); // Apprentice Adventurer's Bow
					st.giveItems(7821, 1); // Apprentice Adventurer's Long Sword
					st.giveItems(17, 500); // Wooden Arrows
					st.giveItems(1060, 50); // Health potions
					st.exitQuest(QuestType.ONE_TIME);
					return "si_illusion_evein_q10322_05.htm";
			}
		}
		else if(npc.getNpcId() == NEWBIE_HELPER)
		{
			switch(st.getCond())
			{
				case 4:
					return "si_newbie_guide_new_q10322_02.htm";
				case 5:
					return "si_newbie_guide_new_q10322_04.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10321_RangerStatus.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;
	}
}