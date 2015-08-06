package dwo.scripts.quests;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.03.13
 * Time: 13:48
 */

public class _00337_AudienceWithTheLandDragon extends Quest
{
	// Квестовые персонажи
	private static final int Moke = 30498;
	private static final int Helton = 30678;
	private static final int Chakiris = 30705;
	private static final int Kaiena = 30720;
	private static final int Gabrielle = 30753;
	private static final int Gilmore = 30754;
	private static final int Theodric = 30755;
	private static final int Kendra = 30851;
	private static final int Orven = 30857;

	// Квестовые монстры
	private static final int Hamrut = 20649;
	private static final int Kranrot = 20650;
	private static final int BloodyQueen = 18001;
	private static final int BloodyQueen1 = 18002;
	private static final int SacrificeOfTheSacrificed = 27171;
	private static final int HaritLizardmanShaman = 20644;
	private static final int HaritLizardmanZealot = 27172;
	private static final int MarshStalker = 20679;
	private static final int MarshDrake = 20680;
	private static final int AbyssJewel1 = 27165;
	private static final int Guard1 = 27168;
	private static final int AbyssJewel2 = 27166;
	private static final int Guard2 = 27169;
	private static final int AbyssJewel3 = 27167;
	private static final int Guard3 = 27170;
	private static final int CaveKeeper = 20277;
	private static final int CaveMaiden = 20287;
	private static final int CaveKeeper1 = 20246;
	private static final int CaveMaiden1 = 20134;

	// Квестовые предметы
	// Items
	private static final int FeatherOfGabrielle = 3852;
	private static final int MarshStalkerHorn = 3853;
	private static final int MarshDrakeTalons = 3854;
	private static final int KranrotSkin = 3855;
	private static final int HamrutLeg = 3856;
	private static final int RemainsOfSacrificed = 3857;
	private static final int TotemOfLandDragon = 3858;
	private static final int FragmentOfAbyssJewel1 = 3859;
	private static final int FragmentOfAbyssJewel2 = 3860;
	private static final int FragmentOfAbyssJewel3 = 3861;
	private static final int MaraFang = 3862;
	private static final int MusfelFang = 3863;
	private static final int MarkOfWatchman = 3864;
	private static final int PortalStone = 3865;
	private static final int HeraldOfSlayer = 3890;

	public _00337_AudienceWithTheLandDragon()
	{
		addStartNpc(Gabrielle);
		addTalkId(Gabrielle, Moke, Helton, Chakiris, Kaiena, Gilmore, Theodric, Kendra, Orven);
		addAttackId(AbyssJewel1, AbyssJewel2, AbyssJewel3);
		addKillId(BloodyQueen, BloodyQueen1, CaveKeeper, CaveMaiden, CaveKeeper1, CaveMaiden1, HaritLizardmanShaman, Hamrut, Kranrot, MarshStalker, MarshDrake, Guard1, Guard2, SacrificeOfTheSacrificed, HaritLizardmanZealot);
		questItemIds = new int[]{
			FeatherOfGabrielle, MarshStalkerHorn, MarshDrakeTalons, KranrotSkin, HamrutLeg, RemainsOfSacrificed,
			TotemOfLandDragon, FragmentOfAbyssJewel1, FragmentOfAbyssJewel2, FragmentOfAbyssJewel3, MaraFang,
			MusfelFang, MarkOfWatchman, HeraldOfSlayer
		};
	}

	public static void main(String[] args)
	{
		new _00337_AudienceWithTheLandDragon();
	}

	@Override
	public int getQuestId()
	{
		return 337;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted())
		{
			switch(npc.getNpcId())
			{
				case AbyssJewel1:
					if(st.getMemoState() == 40000 || st.getMemoState() == 40001)
					{
						if(npc.getCurrentHp() < npc.getMaxHp() * 0.800000 && npc.getAiVar("ai1") == null)
						{
							for(int i = 0; i < 20; i++)
							{
								addSpawn(Guard1, npc.getLoc(), true);
							}
							npc.setAiVar("ai1", 1);
							ThreadPoolManager.getInstance().scheduleAi(new ResetAiVar(npc), 900000);
						}
						if(npc.getCurrentHp() < npc.getMaxHp() * 0.400000)
						{
							st.giveItem(3859);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							ThreadPoolManager.getInstance().scheduleAi(new ResetAiVar(npc), 240000);
						}
					}
					break;
				case AbyssJewel2:
					if(st.getMemoState() == 40000 || st.getMemoState() == 40010)
					{
						if(npc.getCurrentHp() < npc.getMaxHp() * 0.800000 && npc.getAiVar("ai1") == null)
						{
							for(int i = 0; i < 20; i++)
							{
								addSpawn(Guard2, npc.getLoc(), true);
							}
							npc.setAiVar("ai1", 1);
							ThreadPoolManager.getInstance().scheduleAi(new ResetAiVar(npc), 900000);
						}
						if(npc.getCurrentHp() < npc.getMaxHp() * 0.400000 && player.getItemsCount(3860) == 0)
						{
							st.giveItem(3860);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							ThreadPoolManager.getInstance().scheduleAi(new ResetAiVar(npc), 240000);
						}
					}
					break;
				case AbyssJewel3:
					if(st.getMemoState() == 70000)
					{
						if(npc.getCurrentHp() < npc.getMaxHp() * 0.800000 && npc.getAiVar("ai1") == null)
						{
							for(int i = 0; i < 4; i++)
							{
								addSpawn(Guard3, npc.getLoc(), true);
							}
							npc.setAiVar("ai1", 1);
							ThreadPoolManager.getInstance().scheduleAi(new ResetAiVar(npc), 900000);
						}
						if(npc.getCurrentHp() < npc.getMaxHp() * 0.400000 && player.getItemsCount(3861) == 0)
						{
							st.giveItem(3861);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							ThreadPoolManager.getInstance().scheduleAi(new ResetAiVar(npc), 240000);
						}
					}
					break;
			}
		}

		return null;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			qs.giveItem(FeatherOfGabrielle);
			qs.setMemoState(20000);
			return "gabrielle_q0337_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case Gabrielle:
				if(reply == 1)
				{
					return "gabrielle_q0337_03.htm";
				}
				if(reply == 2)
				{
					return "gabrielle_q0337_04.htm";
				}
				if(reply == 3)
				{
					st.takeItems(MarshDrakeTalons, -1);
					st.setMemoState(40000);
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "gabrielle_q0337_09.htm";
				}
				break;
			case Moke:
				if(reply == 1)
				{
					return "warehouse_chief_moke_q0337_02.htm";
				}
				break;
			case Helton:
				if(reply == 1)
				{
					return "blacksmith_helton_q0337_1a.htm";
				}
				break;
			case Gilmore:
				if(reply == 1)
				{
					st.setMemoState(70000);
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "watcher_antaras_gilmore_q0337_03.htm";
				}
				break;
			case Theodric:
				if(reply == 1 && st.getMemoState() == 70000 && player.getItemsCount(FragmentOfAbyssJewel3) >= 1)
				{
					st.giveItem(PortalStone);
					st.takeItems(FragmentOfAbyssJewel3, -1);
					st.takeItems(HeraldOfSlayer, -1);
					st.exitQuest(QuestType.REPEATABLE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "watcher_antaras_theodric_q0337_05.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			switch(npc.getNpcId())
			{
				case HaritLizardmanShaman:
					if((st.getMemoState() == 21110 || st.getMemoState() == 21100 || st.getMemoState() == 21010 || st.getMemoState() == 21000 || st.getMemoState() == 20110 || st.getMemoState() == 20100 || st.getMemoState() == 20010 || st.getMemoState() == 20000) && player.getItemsCount(3858) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							addSpawn(HaritLizardmanZealot, npc.getLoc(), true);
							addSpawn(HaritLizardmanZealot, npc.getLoc(), true);
							addSpawn(HaritLizardmanZealot, npc.getLoc(), true);
						}
					}
					break;
				case HaritLizardmanZealot:
					if((st.getMemoState() == 21110 || st.getMemoState() == 21100 || st.getMemoState() == 21010 || st.getMemoState() == 21000 || st.getMemoState() == 20110 || st.getMemoState() == 20100 || st.getMemoState() == 20010 || st.getMemoState() == 20000) && player.getItemsCount(3858) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3858, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case MarshDrake:
					if((st.getMemoState() == 20111 || st.getMemoState() == 20110 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20011 || st.getMemoState() == 20010 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3854) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3854, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case MarshStalker:
					if((st.getMemoState() == 20111 || st.getMemoState() == 20110 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20011 || st.getMemoState() == 20010 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3853) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3853, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case Kranrot:
					if((st.getMemoState() == 21101 || st.getMemoState() == 21100 || st.getMemoState() == 21001 || st.getMemoState() == 21000 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3855) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3855, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case Hamrut:
					if((st.getMemoState() == 21101 || st.getMemoState() == 21100 || st.getMemoState() == 21001 || st.getMemoState() == 21000 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3856) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3856, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case BloodyQueen:
				case BloodyQueen1:
					if((st.getMemoState() == 21011 || st.getMemoState() == 21010 || st.getMemoState() == 21001 || st.getMemoState() == 21000 || st.getMemoState() == 20011 || st.getMemoState() == 20010 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3857) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
							addSpawn(SacrificeOfTheSacrificed, npc.getLoc(), true);
						}
					}
					break;
				case SacrificeOfTheSacrificed:
					if((st.getMemoState() == 21011 || st.getMemoState() == 21010 || st.getMemoState() == 21001 || st.getMemoState() == 21000 || st.getMemoState() == 20011 || st.getMemoState() == 20010 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3857) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3857, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case Guard1:
					if((st.getMemoState() == 40000 || st.getMemoState() == 40001) && player.getItemsCount(3862) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3862, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case Guard2:
					if((st.getMemoState() == 40000 || st.getMemoState() == 40010) && player.getItemsCount(3863) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							st.giveItems(3863, 1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case CaveKeeper:
				case CaveKeeper1:
				case CaveMaiden:
				case CaveMaiden1:
					if(st.getMemoState() == 70000 && player.getItemsCount(3861) == 0)
					{
						if(Util.checkIfInRange(1500, player, npc, true))
						{
							if(Rnd.get(5) == 0)
							{
								addSpawn(AbyssJewel3, npc.getLoc());
							}
						}
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		L2PcInstance player = st.getPlayer();

		switch(npcId)
		{
			case Gabrielle:
				switch(st.getState())
				{
					case CREATED:
						return player.getLevel() < 50 ? "gabrielle_q0337_01.htm" : "gabrielle_q0337_02.htm";
					case STARTED:
						if(st.getMemoState() >= 20000 && st.getMemoState() < 30000)
						{
							return "gabrielle_q0337_06.htm";
						}
						else if(st.getMemoState() == 30000)
						{
							return "gabrielle_q0337_08.htm";
						}
						else if(st.getMemoState() >= 40000 && st.getMemoState() < 50000)
						{
							return "gabrielle_q0337_10.htm";
						}
						else if(st.getMemoState() == 50000)
						{
							st.giveItems(3890, 1);
							st.takeItems(3852, -1);
							st.takeItems(3864, -1);
							st.setMemoState(60000);
							st.setCond(3);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							return "gabrielle_q0337_11.htm";
						}
						else if(st.getMemoState() == 60000)
						{
							return "gabrielle_q0337_12.htm";
						}
						else if(st.getMemoState() == 70000)
						{
							return "gabrielle_q0337_13.htm";
						}
				}
				break;
			case Kendra:
				if(st.isStarted())
				{
					if((st.getMemoState() == 21110 || st.getMemoState() == 21100 || st.getMemoState() == 21010 || st.getMemoState() == 21000 || st.getMemoState() == 20110 || st.getMemoState() == 20100 || st.getMemoState() == 20010 || st.getMemoState() == 20000) && player.getItemsCount(3858) == 0)
					{
						return "master_kendra_q0337_01.htm";
					}
					else if((st.getMemoState() == 21110 || st.getMemoState() == 21100 || st.getMemoState() == 21010 || st.getMemoState() == 21000 || st.getMemoState() == 20110 || st.getMemoState() == 20100 || st.getMemoState() == 20010 || st.getMemoState() == 20000) && player.getItemsCount(3858) == 1)
					{
						st.giveItems(3864, 1);
						st.takeItems(3858, player.getItemsCount(3858));
						if(st.getMemoState() + 1 == 21111)
						{
							st.setMemoState(30000);
						}
						else
						{
							st.setMemoState(st.getMemoState() + 1);
						}
						return "master_kendra_q0337_02.htm";
					}
					else if(st.getMemoState() == 21101 || st.getMemoState() == 21011 || st.getMemoState() == 21001 || st.getMemoState() == 20111 || st.getMemoState() == 20101 || st.getMemoState() == 20011 || st.getMemoState() == 20001)
					{
						return "master_kendra_q0337_03.htm";
					}
					else if(st.getMemoState() >= 30000)
					{
						return "master_kendra_q0337_04.htm";
					}
				}
				break;
			case Orven:
				if(st.isStarted())
				{
					if((st.getMemoState() == 21011 || st.getMemoState() == 21010 || st.getMemoState() == 21001 || st.getMemoState() == 21000 || st.getMemoState() == 20011 || st.getMemoState() == 20010 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3857) == 0)
					{
						return "highpriest_orven_q0337_01.htm";
					}
					else if((st.getMemoState() == 21011 || st.getMemoState() == 21010 || st.getMemoState() == 21001 || st.getMemoState() == 21000 || st.getMemoState() == 20011 || st.getMemoState() == 20010 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3857) == 1)
					{
						st.giveItems(3864, 1);
						st.takeItems(3857, player.getItemsCount(3857));
						if(st.getMemoState() + 100 == 21111)
						{
							st.setMemoState(30000);
						}
						else
						{
							st.setMemoState(st.getMemoState() + 100);
						}
						return "highpriest_orven_q0337_02.htm";
					}
					else if(st.getMemoState() == 21110 || st.getMemoState() == 21101 || st.getMemoState() == 21100 || st.getMemoState() == 20111 || st.getMemoState() == 20110 || st.getMemoState() == 20101 || st.getMemoState() == 20100)
					{
						return "highpriest_orven_q0337_03.htm";
					}
					else if(st.getMemoState() >= 30000)
					{
						return "highpriest_orven_q0337_04.htm";
					}
				}
				break;
			case Chakiris:
				if(st.isStarted())
				{
					if((st.getMemoState() == 21101 || st.getMemoState() == 21000 || st.getMemoState() == 21100 || st.getMemoState() == 21001 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && (player.getItemsCount(3855) == 0 || player.getItemsCount(3856) == 0))
					{
						return "prefect_chakiris_q0337_01.htm";
					}
					else if((st.getMemoState() == 21101 || st.getMemoState() == 21100 || st.getMemoState() == 21001 || st.getMemoState() == 21000 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3855) == 1 && player.getItemsCount(3856) == 1)
					{
						st.giveItems(3864, 1);
						st.takeItems(3855, player.getItemsCount(3855));
						st.takeItems(3856, player.getItemsCount(3856));
						if(st.getMemoState() + 10 == 21111)
						{
							st.setMemoState(30000);
						}
						else
						{
							st.setMemoState(st.getMemoState() + 10);
						}
						return "prefect_chakiris_q0337_02.htm";
					}
					else if(st.getMemoState() == 21110 || st.getMemoState() == 21011 || st.getMemoState() == 21010 || st.getMemoState() == 20111 || st.getMemoState() == 20110 || st.getMemoState() == 20011 || st.getMemoState() == 20010)
					{
						return "prefect_chakiris_q0337_03.htm";
					}
					else if(st.getMemoState() >= 30000)
					{
						return "prefect_chakiris_q0337_04.htm";
					}
				}
				break;
			case Kaiena:
				if(st.isStarted())
				{
					if((st.getMemoState() == 20111 || st.getMemoState() == 20110 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20010 || st.getMemoState() == 20011 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && (player.getItemsCount(3853) == 0 || player.getItemsCount(3854) == 0))
					{
						return "magister_kaiena_q0337_01.htm";
					}
					else if((st.getMemoState() == 20111 || st.getMemoState() == 20110 || st.getMemoState() == 20101 || st.getMemoState() == 20100 || st.getMemoState() == 20011 || st.getMemoState() == 20010 || st.getMemoState() == 20001 || st.getMemoState() == 20000) && player.getItemsCount(3853) == 1 && player.getItemsCount(3854) == 1)
					{
						st.giveItems(3864, 1);
						st.takeItems(3853, -1);
						st.takeItems(3854, -1);
						if(st.getMemoState() + 1000 == 21111)
						{
							st.setMemoState(30000);
						}
						else
						{
							st.setMemoState(st.getMemoState() + 1000);
						}
						return "magister_kaiena_q0337_02.htm";
					}
					else if(st.getMemoState() == 21110 || st.getMemoState() == 21101 || st.getMemoState() == 21100 || st.getMemoState() == 21011 || st.getMemoState() == 21010 || st.getMemoState() == 21001 || st.getMemoState() == 21000)
					{
						return "magister_kaiena_q0337_03.htm";
					}
					else if(st.getMemoState() >= 30000)
					{
						return "magister_kaiena_q0337_04.htm";
					}
				}
				break;
			case Moke:
				if(st.isStarted())
				{
					if((st.getMemoState() == 40000 || st.getMemoState() == 40001) && (player.getItemsCount(3859) == 0 || player.getItemsCount(3862) == 0))
					{
						return "warehouse_chief_moke_q0337_01.htm";
					}
					else if((st.getMemoState() == 40000 || st.getMemoState() == 40001) && player.getItemsCount(3859) >= 1 && player.getItemsCount(3862) >= 1)
					{
						st.giveItems(3864, 1);
						st.takeItems(3859, player.getItemsCount(3859));
						st.takeItems(3862, player.getItemsCount(3862));

						if(st.getMemoState() == 40001)
						{
							st.setMemoState(50000);
						}
						else
						{
							st.setMemoState(40010);
						}
						return "warehouse_chief_moke_q0337_03.htm";
					}
					else if(st.getMemoState() == 40010)
					{
						return "warehouse_chief_moke_q0337_04.htm";
					}
					else if(st.getMemoState() >= 50000)
					{
						return "warehouse_chief_moke_q0337_05.htm";
					}
				}
				break;
			case Helton:
				if(st.isStarted())
				{
					if((st.getMemoState() == 40000 || st.getMemoState() == 40010) && (player.getItemsCount(3860) == 0 || player.getItemsCount(3863) == 0))
					{
						return "blacksmith_helton_q0337_01.htm";
					}
					else if((st.getMemoState() == 40000 || st.getMemoState() == 40010) && player.getItemsCount(3860) >= 1 && player.getItemsCount(3863) >= 1)
					{
						st.giveItems(3864, 1);
						st.takeItems(3860, player.getItemsCount(3860));
						st.takeItems(3863, player.getItemsCount(3863));
						if(st.getMemoState() == 40010)
						{
							st.setMemoState(50000);
						}
						else
						{
							st.setMemoState(40001);
						}
						return "blacksmith_helton_q0337_02.htm";
					}
					else if(st.getMemoState() == 40001)
					{
						return "blacksmith_helton_q0337_03.htm";
					}
					else if(st.getMemoState() >= 50000)
					{
						return "blacksmith_helton_q0337_04.htm";
					}
				}
				break;
			case Gilmore:
				if(st.isStarted())
				{
					if(st.getMemoState() < 60000)
					{
						return "watcher_antaras_gilmore_q0337_01.htm";
					}
					else if(st.getMemoState() == 60000)
					{
						return "watcher_antaras_gilmore_q0337_02.htm";
					}
					else if(st.getMemoState() == 70000)
					{
						return player.getItemsCount(3861) >= 1 ? "watcher_antaras_gilmore_q0337_05.htm" : "watcher_antaras_gilmore_q0337_04.htm";
					}
				}
				break;
			case Theodric:
				if(st.isStarted())
				{
					if(st.getMemoState() < 60000)
					{
						return "watcher_antaras_theodric_q0337_01.htm";
					}
					else if(st.getMemoState() == 60000)
					{
						return "watcher_antaras_theodric_q0337_02.htm";
					}
					else if(st.getMemoState() == 70000 && player.getItemsCount(3861) == 0)
					{
						return "watcher_antaras_theodric_q0337_03.htm";
					}
					else if(st.getMemoState() == 70000 && player.getItemsCount(3861) >= 1)
					{
						return "watcher_antaras_theodric_q0337_04.htm";
					}
				}
				break;
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 50;
	}

	static class ResetAiVar implements Runnable
	{
		private final L2Npc _npc;

		public ResetAiVar(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			if(_npc != null)
			{
				_npc.unsetAiVar("ai1");
			}
		}
	}
}
