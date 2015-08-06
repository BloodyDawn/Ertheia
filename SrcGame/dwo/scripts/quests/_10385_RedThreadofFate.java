package dwo.scripts.quests;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.06.13
 * Time: 17:58
 */

public class _10385_RedThreadofFate extends Quest
{
	// Квестовые персонажи
	private static final int Rean = 33491;
	private static final int Morelin = 30925;
	private static final int Lania = 33783;
	private static final int HeineWaterSource = 33784;
	private static final int LadyOfTheLake = 31745;
	private static final int Nerupa = 30370;
	private static final int Innocentin = 31328;
	private static final int Enfeux = 31519;
	private static final int Vulkan = 31539;
	private static final int Urn = 31149;
	private static final int Wesley = 30166;
	private static final int DesertedDwarvenHouse = 33788;
	private static final int PaagrioTemple = 33787;
	private static final int AltarofShilen = 33785;
	private static final int ShilensMessenger = 27492;
	private static final int CaveofSouls = 33789;
	private static final int MotherTree = 33786;
	private static final int Darin = 33748;
	private static final int Roxxy = 33749;
	private static final int BiotinHighPriest = 30031;
	private static final int MysteriousDarkKnight = 33751;

	// Квестовые предметы
	private static final int MysteriosLetter = 36072;
	private static final int Waterfromthegardenofeva = 36066;
	private static final int ClearestWater = 36067;
	private static final int BrightestLight = 36068;
	private static final int PurestSoul = 36069;
	private static final int VulkanGold = 36113;
	private static final int VulkanSilver = 36114;
	private static final int VulkanFire = 36115;
	private static final int FiercestFlame = 36070;
	private static final int FondestHeart = 36071;
	private static final int SoEToDwarvenVillage = 20376;
	private static final int DimensionalDiamond = 7562;

	// Квестовые умения
	private static final int SkillWater = 9579;
	private static final int SkillLight = 9580;
	private static final int SkillSoul = 9581;
	private static final int SkillFlame = 9582;
	private static final int SkillLove = 9583;

	// Квестовые зоны
	private static final int EXIT_ZONE_ID_1 = 400103;
	private static final int EXIT_ZONE_ID_2 = 400104;

	private static final Location ENTER_POINT = new Location(210808, 13432, -3748);

	public _10385_RedThreadofFate()
	{
		addStartNpc(Rean);
		addTalkId(Rean, Morelin, Lania, HeineWaterSource, LadyOfTheLake, Nerupa, Innocentin, Enfeux, Vulkan, Urn, Wesley, DesertedDwarvenHouse, PaagrioTemple, AltarofShilen, ShilensMessenger, CaveofSouls, MotherTree, Darin, Roxxy, BiotinHighPriest, MysteriousDarkKnight);
		addKillId(ShilensMessenger);
		addSocialSeeId(Lania);
		addAskId(MysteriousDarkKnight, 10385); // Квест идет сразу от стартового диалога !!
		addSkillSeeId(MotherTree, AltarofShilen, CaveofSouls, PaagrioTemple, DesertedDwarvenHouse);
		addEnterZoneId(EXIT_ZONE_ID_1, EXIT_ZONE_ID_2);
		questItemIds = new int[]{
			MysteriosLetter, Waterfromthegardenofeva, ClearestWater, BrightestLight, PurestSoul, VulkanGold,
			VulkanSilver, VulkanFire, FiercestFlame, FondestHeart, SoEToDwarvenVillage, DimensionalDiamond
		};
	}

	public static void main(String[] args)
	{
		new _10385_RedThreadofFate();
	}

	@Override
	public int getQuestId()
	{
		return 10385;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getClass());
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.giveItem(MysteriosLetter);
			return "sub_class_ellenia_q10385_05.htm";
		}
		if(event.equals("destroy_instance"))
		{
			if(player.getInstanceController().isInInstance())
			{
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				QuestState st = player.getQuestState(getClass());
				if(st != null && st.isStarted() && st.getCond() == 21)
				{
					st.setCond(22);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				}
				player.teleToLocation(-113656, 246040, -3724, 0);
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int cond = st.getCond();
		if(npc.getNpcId() == Rean)
		{
			if(reply == 1)
			{
				return "sub_class_ellenia_q10385_04.htm";
			}
			else if(reply == 2 && cond == 22)
			{
				st.giveItems(DimensionalDiamond, 40);
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "sub_class_ellenia_q10385_08.htm";
			}
		}
		else if(npc.getNpcId() == Morelin)
		{
			if(reply == 1 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "highpriestess_morelyn_q10385_02.htm";
			}
		}
		else if(npc.getNpcId() == Lania)
		{
			if(reply == 1 && cond == 2)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "ranya2_q10385_02.htm";
			}
			else if(reply == 2)
			{
				player.teleToLocation(80744, 254664, -10389);
				return null;
			}
		}
		else if(npc.getNpcId() == HeineWaterSource)
		{
			if(reply == 1 && cond == 5)
			{
				st.giveItem(Waterfromthegardenofeva);
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				player.teleToLocation(143224, 43928, -3063);
				return null;
			}
		}
		else if(npc.getNpcId() == LadyOfTheLake)
		{
			if(cond == 6)
			{
				switch(reply)
				{
					case 1:
						return "lady_of_the_lake_q10385_02.htm";
					case 2:
						st.takeItems(Waterfromthegardenofeva, -1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "lady_of_the_lake_q10385_03.htm";
					case 3:
						return "lady_of_the_lake_q10385_04.htm";
					case 4:
						return "lady_of_the_lake_q10385_05.htm";
					case 5:
						st.giveItem(ClearestWater);
						st.setCond(7);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						player.teleToLocation(172440, 90312, -2011);
						return null;
				}
			}
		}
		else if(npc.getNpcId() == Nerupa)
		{
			if(cond == 7)
			{
				switch(reply)
				{
					case 1:
						return "nerupa_q10385_02.htm";
					case 2:
						return "nerupa_q10385_03.htm";
					case 3:
						st.setCond(8);
						st.giveItem(BrightestLight);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "nerupa_q10385_04.htm";
				}
			}
		}
		else if(npc.getNpcId() == Enfeux)
		{
			if(reply == 1 && cond == 8)
			{
				st.giveItem(PurestSoul);
				st.setCond(9);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "enfeux_q10385_02.htm";
			}
		}
		else if(npc.getNpcId() == Innocentin)
		{
			if(reply == 1 && cond == 9)
			{
				st.setCond(10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "highpriest_innocentin_q10385_02.htm";
			}
		}
		else if(npc.getNpcId() == Vulkan)
		{
			if(cond == 10)
			{
				switch(reply)
				{
					case 1:
						return "warsmith_vulcan_q10385_02.htm";
					case 2:
						return "warsmith_vulcan_q10385_03.htm";
					case 3:
						st.giveItem(VulkanGold);
						st.giveItem(VulkanSilver);
						st.giveItem(VulkanFire);
						st.setCond(11);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "warsmith_vulcan_q10385_04.htm";
				}
			}
			else if(cond == 13)
			{
				switch(reply)
				{
					case 4:
						return "warsmith_vulcan_q10385_07.htm";
					case 5:
						return "warsmith_vulcan_q10385_08.htm";
					case 6:
						st.giveItem(FiercestFlame);
						st.giveItem(FondestHeart);
						st.giveItem(SoEToDwarvenVillage);
						st.setCond(14);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "warsmith_vulcan_q10385_09.htm";
				}
			}
		}
		else if(npc.getNpcId() == Urn)
		{
			if(reply == 1 && cond == 11)
			{
				st.takeItems(VulkanGold, -1);
				st.takeItems(VulkanSilver, -1);
				st.takeItems(VulkanFire, -1);
				st.setCond(12);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "alchemical_mixing_jar_q10385_02.htm";
			}
		}
		else if(npc.getNpcId() == Wesley)
		{
			if(reply == 1 && cond == 12)
			{
				return "bandor_q10385_02.htm";
			}
			else if(reply == 2 && (cond == 12 || cond == 13))
			{
				if(cond == 12)
				{
					st.setCond(13);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				player.teleToLocation(180168, -111720, -5856);
				return "bandor_q10385_03.htm";
			}
		}
		else if(npc.getNpcId() == Darin)
		{
			if(reply == 1)
			{
				return "darin_q10385_02.htm";
			}
			else if(reply == 2)
			{
				player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1802018), ExShowScreenMessage.TOP_CENTER, 4500));
				return "darin_q10385_03.htm";
			}
		}
		else if(npc.getNpcId() == Roxxy)
		{
			if(reply == 1 && st.getCond() == 19)
			{
				st.setCond(20);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "roxxy_q10385_02.htm";
			}
		}
		else if(npc.getNpcId() == BiotinHighPriest)
		{
			if(reply == 1 && st.getCond() == 20)
			{
				return "quilt_q10385_02.htm";
			}
			else if(reply == 2 && st.getCond() == 20)
			{
				player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1802019), ExShowScreenMessage.TOP_CENTER, 4500));
				st.setCond(21);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "quilt_q10385_03.htm";
			}
			else
			{
				return "quilt_q10385_04.htm";
			}
		}
		else if(npc.getNpcId() == MysteriousDarkKnight)
		{
			if(cond == 21)
			{
				switch(reply)
				{
					case 1:
						return "mysterious_dark_knight_q10385_01.htm";
					case 2:
						return "mysterious_dark_knight_q10385_02.htm";
					case 3:
						st.setCond(22);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						player.showQuestMovie(ExStartScenePlayer.SCENE_SUB_QUEST);
						startQuestTimer("destroy_instance", 25000, npc, player);
						return null;
				}
			}

		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == getQuestId())
		{
			QuestState qs = player.getQuestState(getClass());
			if(qs != null)
			{
				return onAsk(player, npc, qs, reply);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && npc.getNpcId() == ShilensMessenger && st.getCond() == 16)
		{
			st.setCond(17);
			st.takeItems(ClearestWater, -1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == Rean)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "sub_class_ellenia_q10385_03.htm";
				case CREATED:
					if(canBeStarted(player))
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/sub_class_ellenia_q10385_02.htm");
						content = content.replace("<?name?>", player.getName());
						return content;
					}
					else
					{
						return "sub_class_ellenia_q10385_01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "sub_class_ellenia_q10385_06.htm";
					}
					else if(st.getCond() == 22)
					{
						return "sub_class_ellenia_q10385_07.htm";
					}
			}
		}
		else if(npc.getNpcId() == Morelin)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "highpriestess_morelyn_q10385_01.htm";
				}
				else if(st.getCond() == 2)
				{
					return "highpriestess_morelyn_q10385_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Lania)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "ranya2_q10385_01.htm";
				}
				else if(st.getCond() == 3)
				{
					return "ranya2_q10385_02.htm";
				}
				else if(st.getCond() == 4)
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "ranya2_q10385_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == HeineWaterSource)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 5)
				{
					return "heine_water_source_q10385_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == LadyOfTheLake)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 6)
				{
					return "lady_of_the_lake_q10385_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == Nerupa)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 7)
				{
					return "nerupa_q10385_01.htm";
				}
				else if(st.getCond() == 8)
				{
					return "nerupa_q10385_05.htm";
				}
			}
		}
		else if(npc.getNpcId() == Enfeux)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 8)
				{
					return "enfeux_q10385_01.htm";
				}
				else if(st.getCond() == 9)
				{
					return "enfeux_q10385_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Innocentin)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 9)
				{
					return "highpriest_innocentin_q10385_01.htm";
				}
				else if(st.getCond() == 10)
				{
					return "highpriest_innocentin_q10385_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Vulkan)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 10:
						return "warsmith_vulcan_q10385_01.htm";
					case 11:
						return "warsmith_vulcan_q10385_05.htm";
					case 13:
						return "warsmith_vulcan_q10385_06.htm";
					case 14:
						return "warsmith_vulcan_q10385_10.htm";
				}
			}
		}
		else if(npc.getNpcId() == Urn)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 11)
				{
					return "alchemical_mixing_jar_q10385_01.htm";
				}
				else if(st.getCond() == 12)
				{
					return "alchemical_mixing_jar_q10385_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Wesley)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 12)
				{
					return "bandor_q10385_01.htm";
				}
				else if(st.getCond() == 13)
				{
					return "bandor_q10385_04.htm";
				}
			}
		}
		else if(npc.getNpcId() == Darin)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 19)
				{
					return "darin_q10385_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == Roxxy)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 19)
				{
					return "roxxy_q10385_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == BiotinHighPriest)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 20)
				{
					return "quilt_q10385_01.htm";
				}
				return "quilt_q10385_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onSocialSee(L2Npc npc, L2PcInstance player, L2Object target, int socialId)
	{
		if(target != null && target instanceof L2Npc && socialId == 2)
		{
			QuestState st = player.getQuestState(getClass());
			if(st != null && st.isStarted() && st.getCond() == 3)
			{
				L2Npc npcToSocial = (L2Npc) target;
				if(npcToSocial.getNpcId() == Lania)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(caster != null && targets.length > 0 && targets[0].equals(npc))
		{
			QuestState st = caster.getQuestState(getClass());
			if(st != null && st.isStarted())
			{
				switch(skill.getId())
				{
					case SkillLove:
						if(npc.getNpcId() == DesertedDwarvenHouse && st.getCond() == 14)
						{
							st.takeItems(FondestHeart, -1);
							st.setCond(15);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						break;
					case SkillFlame:
						if(npc.getNpcId() == PaagrioTemple && st.getCond() == 15)
						{
							st.takeItems(FiercestFlame, -1);
							st.setCond(16);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						break;
					case SkillLight:
						if(npc.getNpcId() == AltarofShilen && st.getCond() == 16)
						{
							st.takeItems(BrightestLight, -1);
							caster.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(18564), ExShowScreenMessage.TOP_CENTER, 4500));
							L2Npc mob = st.addSpawn(ShilensMessenger, 28760, 11032, -4252);
							((L2MonsterInstance) mob).attackCharacter(caster);
						}
						break;
					case SkillSoul:
						if(npc.getNpcId() == CaveofSouls && st.getCond() == 17)
						{
							st.takeItems(PurestSoul, -1);
							st.setCond(18);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						break;
					case SkillWater:
						if(npc.getNpcId() == MotherTree && st.getCond() >= 18)
						{
							enterInstance(caster);
							if(st.getCond() == 18)
							{
								st.setCond(19);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character.isPlayer())
		{
			L2PcInstance player = (L2PcInstance) character;
			QuestState st = player.getQuestState(getClass());
			Instance instance = InstanceManager.getInstance().getInstance(player.getInstanceId());
			if(st != null && instance != null && instance.getAllByNpcId(MysteriousDarkKnight, true).isEmpty())
			{
				if(st.isStarted() && st.getCond() == 21)
				{
					Location spawnLocation = zone.getId() == EXIT_ZONE_ID_1 ? new Location(210632, 15576, -3754) : new Location(209399, 15072, -3735);
					addSpawn(MysteriousDarkKnight, spawnLocation, player.getInstanceId());
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 85 && player.isAwakened();

	}

	private void enterInstance(L2PcInstance player)
	{
		InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof RTFWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTER_POINT, world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("TalkingIsland.xml");

			world = new RTFWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.TALKIN_ISLAND.getId();
			InstanceManager.getInstance().addWorld(world);
			world.allowed.add(player.getObjectId());
			player.teleToInstance(ENTER_POINT, instanceId);
		}
	}

	public class RTFWorld extends InstanceManager.InstanceWorld
	{
	}
}