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

/**
 * @author ANZO
 * 08.04.2010
 */
public class _10270_BirthOfTheSeed extends Quest
{
	// Нпцшечки
	private static final int PLENOS = 32563;
	private static final int ARTIUS = 32559;
	private static final int GINBY = 32566;
	private static final int LELIKIA = 32567;
	// РБ
	private static final int KLODEKUS = 25665;
	private static final int KLANIKUS = 25666;
	private static final int COHEMENES = 25634;
	private static final int[] MOBS = {KLODEKUS, KLANIKUS, COHEMENES};
	// Квестовые вещи
	private static final int KLODEKUS_BADGE = 13868;
	private static final int KLANIKUS_BADGE = 13869;
	private static final int LICH_CRYSTAL = 13870;
	// Телепорты
	private static final Location ENTRY_POINT = new Location(-23759, -8962, -5384);
	private static final Location EXIT_POINT = new Location(-184991, 242826, 1576);

	public _10270_BirthOfTheSeed()
	{

		addStartNpc(PLENOS);
		addTalkId(PLENOS, ARTIUS, GINBY);
		addTalkId(LELIKIA); // Онферст наверное сделать
		addKillId(MOBS);

		questItemIds = new int[]{KLODEKUS_BADGE, KLANIKUS_BADGE, LICH_CRYSTAL};
	}

	public static void main(String[] args)
	{
		new _10270_BirthOfTheSeed();
	}

	@Override
	public int getQuestId()
	{
		return 10270;
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
			case "quest_accept":
				if(!st.isStarted())
				{
					st.startQuest();
					return "wharf_soldier_plenos_q10270_05.htm";
				}
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		switch(npc.getNpcId())
		{
			case LELIKIA:
				switch(reply)
				{
					case 1:
						return "silen_priest_relrikia_q10270_02.htm";
					case 2:
						return "silen_priest_relrikia_q10270_03.htm";
					// TODO: silen_priest_relrikia_q10270_04, как-то связан Экимус
					case 3:
						if(qs.getCond() == 4)
						{
							qs.setCond(5);
							InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(30000);
							return "silen_priest_relrikia_q10270_05.htm";
						}
						break;
					case 4:
						if(qs.getCond() == 5)
						{
							InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(100);
							return "silen_priest_relrikia_q10270_07.htm";
						}
						break;
				}
				break;
			case GINBY:
				switch(reply)
				{
					case 1:
						if(qs.getCond() == 4)
						{
							qs.set("ginby_ev", "1");
							return "soldier_jinbi_q10270_03.htm";
						}
						break;
					case 2:
						if(qs.getCond() == 4)
						{
							if(qs.getQuestItemsCount(PcInventory.ADENA_ID) >= 10000)
							{
								qs.takeAdena(10000);
								return "soldier_jinbi_q10270_05.htm";
							}
							else
							{
								return "soldier_jinbi_q10270_04a.htm";
							}
						}
						break;
					case 3:
					{
						// TODO: soldier_jinbi_q10270_09, проверка на возможность входа в инстанс (наверное, связано с ограничением на одновременное кол-во человек в инстах одного типа)
						InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
						return world instanceof BirthOTS ? "soldier_jinbi_q10270_10.htm" : "soldier_jinbi_q10270_06.htm";
					}
					case 4:
						if(qs.getCond() == 4)
						{
							enterInstance(player, 117);
							InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
							return world instanceof BirthOTS ? "soldier_jinbi_q10270_11.htm" : "soldier_jinbi_q10270_08.htm";
						}
						break;
				}
				break;
			case ARTIUS:
				switch(reply)
				{
					case 1:
						if(qs.getCond() == 1)
						{
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							qs.setCond(2);
							return "warmage_artius_q10270_03.htm";
						}
						break;
					case 2:
						if(qs.getCond() == 3)
						{
							return "warmage_artius_q10270_08.htm";
						}
						break;
					case 3:
						if(qs.getCond() == 3)
						{
							qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							qs.setCond(4);
							return "warmage_artius_q10270_09.htm";
						}
						break;
					case 10:
						if(qs.getCond() == 5)
						{
							qs.addExpAndSp(625343, 48222);
							qs.giveAdena(133590, true);
							qs.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							qs.unset("cond");
							qs.exitQuest(QuestType.ONE_TIME);
							return "warmage_artius_q10270_13.htm";
						}
						break;
				}
				break;
			case PLENOS:
				switch(reply)
				{
					case 1:
						return "wharf_soldier_plenos_q10270_04.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		switch(npc.getNpcId())
		{
			case KLODEKUS:
				if(player.isInParty())
				{
					for(L2PcInstance plr : player.getParty().getMembers())
					{
						QuestState st = plr.getQuestState(getClass());
						if(st != null && st.getCond() == 1)
						{
							st.giveItems(KLODEKUS_BADGE, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							if(st.hasQuestItems(KLODEKUS_BADGE) && st.hasQuestItems(KLANIKUS_BADGE) && st.hasQuestItems(LICH_CRYSTAL))
							{
								st.setCond(2);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
					}
				}
				else
				{
					QuestState st = player.getQuestState(getClass());
					if(st != null && st.getCond() == 1)
					{
						st.giveItems(KLODEKUS_BADGE, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						if(st.hasQuestItems(KLODEKUS_BADGE) && st.hasQuestItems(KLANIKUS_BADGE) && st.hasQuestItems(LICH_CRYSTAL))
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
				break;
			case KLANIKUS:
				if(player.isInParty())
				{
					for(L2PcInstance plr : player.getParty().getMembers())
					{
						QuestState st = plr.getQuestState(getClass());
						if(st != null && st.getCond() == 1)
						{
							st.giveItems(KLANIKUS_BADGE, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							if(st.hasQuestItems(KLODEKUS_BADGE) && st.hasQuestItems(KLANIKUS_BADGE) && st.hasQuestItems(LICH_CRYSTAL))
							{
								st.setCond(2);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
					}
				}
				else
				{
					QuestState st = player.getQuestState(getClass());
					if(st != null && st.getCond() == 1)
					{
						st.giveItems(KLANIKUS_BADGE, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						if(st.hasQuestItems(KLODEKUS_BADGE) && st.hasQuestItems(KLANIKUS_BADGE) && st.hasQuestItems(LICH_CRYSTAL))
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
				break;
			case COHEMENES:
				if(player.isInParty())
				{
					for(L2PcInstance plr : player.getParty().getMembers())
					{
						QuestState st = plr.getQuestState(getClass());
						if(st != null && st.getCond() == 1)
						{
							st.giveItems(LICH_CRYSTAL, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							if(st.hasQuestItems(KLODEKUS_BADGE) && st.hasQuestItems(KLANIKUS_BADGE) && st.hasQuestItems(LICH_CRYSTAL))
							{
								st.setCond(2);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
					}
				}
				else
				{
					QuestState st = player.getQuestState(getClass());
					if(st != null && st.getCond() == 1)
					{
						st.giveItems(LICH_CRYSTAL, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						if(st.hasQuestItems(KLODEKUS_BADGE) && st.hasQuestItems(KLANIKUS_BADGE) && st.hasQuestItems(LICH_CRYSTAL))
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
				break;
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();
		int npcId = npc.getNpcId();
		QuestStateType id = st.getState();

		switch(id)
		{
			case COMPLETED:
				if(npcId == PLENOS)
				{
					return "wharf_soldier_plenos_q10270_03.htm";
				}
				if(npcId == ARTIUS)
				{
					return "warmage_artius_q10270_02.htm";
				}
				if(npcId == GINBY)
				{
					return "soldier_jinbi_q10270_02.htm";
				}
				break;
			case CREATED:
				if(player.getLevel() > 75)
				{
					if(npcId == PLENOS)
					{
						return "wharf_soldier_plenos_q10270_01.htm";
					}
				}
				else
				{
					return "wharf_soldier_plenos_q10270_02.htm";
				}
				break;
			case STARTED:
				if(npcId == PLENOS)
				{
					if(cond == 1)
					{
						return "wharf_soldier_plenos_q10270_06.htm";
					}
				}
				else if(npcId == ARTIUS)
				{
					switch(cond)
					{
						case 1:
							return "warmage_artius_q10270_01.htm";
						case 2:
							if(st.getQuestItemsCount(KLODEKUS_BADGE) > 0 && st.getQuestItemsCount(KLANIKUS_BADGE) > 0 && st.getQuestItemsCount(LICH_CRYSTAL) > 0)
							{
								st.setCond(3);
								return "warmage_artius_q10270_06.htm";
							}
							else
							{
								return st.getQuestItemsCount(KLODEKUS_BADGE) > 0 || st.getQuestItemsCount(KLANIKUS_BADGE) > 0 || st.getQuestItemsCount(LICH_CRYSTAL) > 0 ? "warmage_artius_q10270_05.htm" : "warmage_artius_q10270_04.htm";
							}
						case 3:
							return "warmage_artius_q10270_07.htm";
						case 4:
							return "warmage_artius_q10270_10.htm";
						case 5:
							return "warmage_artius_q10270_12.htm";
					}
				}
				else if(npcId == GINBY)
				{
					if(cond == 4)
					{
						return st.getInt("ginby_ev") != 1 ? "soldier_jinbi_q10270_01.htm" : "soldier_jinbi_q10270_03.htm";
					}
					else if(cond >= 5)
					{
						return "soldier_jinbi_q10270_12.htm";
					}
				}
				else if(npcId == LELIKIA)
				{
					if(cond == 4)
					{
						return "silen_priest_relrikia_q10270_01.htm";
					}
					else if(cond >= 5)
					{
						return "silen_priest_relrikia_q10270_06.htm";
					}
				}
				break;
		}
		return null;
	}

	private void enterInstance(L2PcInstance player, int inst)
	{
		synchronized(this)
		{
			if(inst == 117) // Secret Area in the Keucereus Fortress
			{
				InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				if(world instanceof BirthOTS)
				{
					player.teleToInstance(ENTRY_POINT, world.instanceId);
					return;
				}

				world = new BirthOTS();
				world.instanceId = InstanceManager.getInstance().createDynamicInstance(null);
				world.templateId = InstanceZoneId.SECRET_AREA_IN_THE_KEUCEREUS_FORTRESS_1.getId();
				Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
				int time = 3600000;
				instance.setDuration(time);
				instance.setEmptyDestroyTime(10);
				instance.setSpawnLoc(EXIT_POINT);
				InstanceManager.getInstance().addWorld(world);
				instance.setName("Secret Area in the Keucereus Fortress");
				addSpawn(LELIKIA, -23966, -8963, -5389, 0, false, 0, false, world.instanceId);
				player.teleToInstance(ENTRY_POINT, world.instanceId);
			}
		}
	}

	private class BirthOTS extends InstanceWorld
	{
	}
}
