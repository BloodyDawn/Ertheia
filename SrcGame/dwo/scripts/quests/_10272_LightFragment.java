package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _10272_LightFragment extends Quest
{
	// NPC
	private static final int ORBYU = 32560;
	private static final int ARTIUS = 32559;
	private static final int GINBY = 32566;
	private static final int LELIKIA = 32567;
	private static final int LEKON = 32557;
	// Items
	private static final int DOCUMENT = 13852;
	private static final int DARKNESS_FRAGMENT = 13853;
	private static final int LIGHT_FRAGMENT = 13854;
	private static final int SACRED_FRAGMENT = 13855;
	// Телепорты
	private static final Location ENTRY_POINT = new Location(-23759, -8962, -5384);
	private static final Location EXIT_LOCATION = new Location(-184991, 242826, 1576);
	private static int[] MOBS = {
		22537, 22538, 22539, 22541, 22542, 22543, 22544, 22547, 22548, 22549, 22559, 22560, 22561, 22562, 22563, 22564,
		22566, 22567,
	};

	public _10272_LightFragment()
	{
		addStartNpc(ORBYU);
		addTalkId(ORBYU, ARTIUS, GINBY, LELIKIA, LEKON);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _10272_LightFragment();
	}

	private void enterInstance(L2PcInstance player, int inst)
	{
		synchronized(this)
		{
			if(inst == 118) //Secret Area in the Keucereus Fortress2
			{
				InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				if(world instanceof LightF)
				{
					player.teleToInstance(ENTRY_POINT, world.instanceId);
					return;
				}
				world = new LightF();
				world.instanceId = InstanceManager.getInstance().createDynamicInstance(null);
				world.templateId = InstanceZoneId.SECRET_AREA_IN_THE_KEUCEREUS_FORTRESS_2.getId();
				Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
				int time = 3600000;
				instance.setDuration(time);
				instance.setEmptyDestroyTime(10);
				instance.setSpawnLoc(EXIT_LOCATION);
				InstanceManager.getInstance().addWorld(world);
				instance.setName("Secret Area in the Keucereus Fortress");
				addSpawn(LELIKIA, -23966, -8963, -5389, 0, false, 0, false, world.instanceId);
				player.teleToInstance(ENTRY_POINT, world.instanceId);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 10272;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		switch(event)
		{
			case "quest_accept":
				if(!st.isStarted())
				{
					st.startQuest();
					st.giveItems(DOCUMENT, 1);
					return "wharf_soldier_orbiu_q10272_06.htm";
				}
		}

		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		switch(npc.getNpcId())
		{
			case ORBYU:
				switch(reply)
				{
					case 1:
						return "wharf_soldier_orbiu_q10272_05.htm";
				}
				break;
			case ARTIUS:
				switch(reply)
				{
					case 1:
						return "warmage_artius_q10272_02.htm";
					case 2:
						if(qs.getCond() == 2)
						{
							qs.setCond(3);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "warmage_artius_q10272_03.htm";
						}
						break;
					case 3:
						return "warmage_artius_q10272_05.htm";
					case 4:
						return "warmage_artius_q10272_06.htm";
					case 10:
						return "warmage_artius_q10272_08.htm";
					case 21:
						return "warmage_artius_q10272_11.htm";
					case 22:
						if(qs.getCond() == 4)
						{
							qs.setCond(5);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "warmage_artius_q10272_12.htm";
						}
						break;
				}
				break;
			case GINBY:
				switch(reply)
				{
					case 1:
						return "soldier_jinbi_q10272_03.htm";
					case 2:
						if(qs.getCond() == 3)
						{
							if(qs.getInt("ginby_lf") == 1)
							{
								return "soldier_jinbi_q10272_05.htm";
							}
							else if(qs.getQuestItemsCount(PcInventory.ADENA_ID) >= 10000)
							{
								qs.set("ginby_lf", "1");
								qs.takeAdena(10000);
								return "soldier_jinbi_q10272_05.htm";
							}
							else
							{
								return "soldier_jinbi_q10272_04a.htm";
							}
						}

						break;
					case 3:
						if(qs.getInt("ginby_lf") == 1)
						{
							InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
							if(world instanceof LightF)
							{
								return "soldier_jinbi_q10272_09.htm";
							}
							return "soldier_jinbi_q10272_06.htm";
						}
						break;
					case 4:
						if(qs.getInt("ginby_lf") == 1)
						{
							InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
							if(world instanceof LightF)
							{
								return "soldier_jinbi_q10272_10.htm";
							}
							// TODO: soldier_jinbi_q10272_08.htm, связано с макс. загруженностью инстанса
							enterInstance(player, 117);
							return "soldier_jinbi_q10272_07.htm";
						}
						break;
				}
			case LELIKIA:
				switch(reply)
				{
					case 1:
						return "silen_priest_relrikia_q10272_02.htm";
					case 2:
						return "silen_priest_relrikia_q10272_03.htm";
					case 3:
						if(qs.getCond() == 3)
						{
							qs.setCond(4);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "silen_priest_relrikia_q10272_04.htm";
						}
						if(qs.getCond() > 3)
						{
							return "silen_priest_relrikia_q10272_05.htm";
						}
					case 4:
						if(qs.getCond() == 3 && qs.getInt("ginby_lf") == 1)
						{
							InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(100);
							return "silen_priest_relrikia_q10272_06.htm";
						}
						break;
				}
				break;
			case LEKON:
				switch(reply)
				{
					case 1:

						return "engineer_recon_q10272_02.htm";
					case 2:
						if(qs.getCond() == 7)
						{
							if(qs.getQuestItemsCount(LIGHT_FRAGMENT) < 1)
							{
								return "engineer_recon_q10272_04.htm";
							}
							else
							{
								qs.setCond(8);
								qs.takeItems(LIGHT_FRAGMENT, -1);
								qs.giveItems(SACRED_FRAGMENT, 1);
								return "engineer_recon_q10272_03.htm";
							}
						}
						break;
				}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(ArrayUtils.contains(MOBS, npcId) && cond == 5 && Rnd.getChance(80))
		{
			st.giveItems(DARKNESS_FRAGMENT, 1);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		String htmltext = getNoQuestMsg(player);
		QuestStateType id = st.getState();
		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(npcId == ORBYU)
		{
			if(id == COMPLETED)
			{
				htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			}
			else if(cond == 0)
			{
				QuestState qs = player.getQuestState(_10271_TheEnvelopingDarkness.class);
				if(qs == null || !qs.isCompleted())
				{
					htmltext = "wharf_soldier_orbiu_q10272_02.htm";
				}
				else if(id == CREATED && player.getLevel() >= 75)
				{
					htmltext = "wharf_soldier_orbiu_q10272_01.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
				else if(player.getLevel() < 75)
				{
					htmltext = "wharf_soldier_orbiu_q10272_03.htm";
				}
				else if(st.isCompleted())
				{
					htmltext = "wharf_soldier_orbiu_q10272_04.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "wharf_soldier_orbiu_q10272_07.htm";
			}
		}
		else if(npcId == ARTIUS)
		{
			if(st.isCompleted())
			{
				return "warmage_artius_q10272_19.htm";
			}

			switch(cond)
			{
				case 1:
					if(st.hasQuestItems(DOCUMENT))
					{
						st.takeItems(DOCUMENT, 1);
					}

					st.setCond(2);
					htmltext = "warmage_artius_q10272_01.htm";
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					break;
				case 2:
					htmltext = "warmage_artius_q10272_04.htm";
					break;
				case 3:
					htmltext = "warmage_artius_q10272_09.htm";
					break;
				case 4:
					htmltext = "warmage_artius_q10272_10.htm";
					break;
				case 5:
					if(st.getQuestItemsCount(DARKNESS_FRAGMENT) <= 0)
					{
						htmltext = "warmage_artius_q10272_13.htm";
					}
					else if(st.getQuestItemsCount(DARKNESS_FRAGMENT) < 100)
					{
						htmltext = "warmage_artius_q10272_14.htm";
					}
					else
					{
						st.setCond(6);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						htmltext = "warmage_artius_q10272_15.htm";
					}
					break;
				case 6:
					if(st.getQuestItemsCount(LIGHT_FRAGMENT) >= 100)
					{
						st.setCond(7);
						htmltext = "warmage_artius_q10272_17.htm";
					}
					else
					{
						htmltext = "warmage_artius_q10272_16.htm";
					}
					break;
				case 8:
					htmltext = "warmage_artius_q10272_18.htm";
					st.unset("cond");
					st.unset("ginby_lf");
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.giveAdena(600000, true);
					st.addExpAndSp(2219330, 2458030);
					break;
			}
		}
		else if(npcId == GINBY)
		{
			if(cond == 3)
			{
				htmltext = "soldier_jinbi_q10272_01.htm";
			}
			else
			{
				htmltext = cond >= 3 ? "soldier_jinbi_q10272_11.htm" : "soldier_jinbi_q10272_02.htm";
			}
		}
		else if(npcId == LELIKIA)
		{
			if(cond == 3)
			{
				htmltext = "silen_priest_relrikia_q10272_01.htm";
			}
		}
		else if(npcId == LEKON && cond == 7 && st.getQuestItemsCount(LIGHT_FRAGMENT) >= 100)
		{
			htmltext = "engineer_recon_q10272_01.htm";
		}
		return htmltext;
	}

	private class LightF extends InstanceWorld
	{
	}
}