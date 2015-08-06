package dwo.scripts.services;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TheSpiritOfPigs extends Quest
{
	private static final int[][] LUCKPY_TRIGGER_MOB_IDS = {
		{20589, 20590, 20591, 20592, 20593, 20594, 20595, 20596, 20597, 20598, 20599}, {
		21520, 21521, 21522, 21523, 21524, 21525, 21526, 21527, 21528, 21529, 21530, 21531, 21532, 21533, 21534, 21535,
		21536, 21537, 21538, 21539, 21540, 21541, 21542, 21543, 21544, 21545, 21546, 21547, 21548, 21549, 21550, 21551,
		21552, 21553, 21554, 21555, 21556, 21557, 21558, 21559, 21560, 21561, 21562, 21563, 21564, 21565, 21566, 21567,
		21568, 21569, 21570, 21571, 21572, 21573, 21574, 21575, 21576, 21577, 21578, 21579, 21580, 21581, 21582, 21583,
		21584, 21585, 21586, 21587, 21588, 21589, 21590, 21591, 21592, 21593, 21594, 21595, 21596, 21597, 21598, 21599,
		21600, 21601
	}, {
		18873, 18874, 18875, 18876, 18877, 18878, 18879, 18880, 18881, 18882, 18883, 18884, 18885, 18886, 18887, 18888,
		18889, 18890, 18891, 18892, 18893, 18894, 18895, 18896, 18897, 18898, 18899, 18900, 18901, 18902, 18903, 18904,
		18905, 18906, 18907, 22196, 22197, 22198, 22199, 22200, 22201, 22202, 22203, 22204, 22205, 22206, 22207, 22208,
		22209, 22210, 22211, 22212, 22213, 22214, 22215, 22216, 22217, 22218, 22219, 22220, 22221, 22222, 22223, 22224,
		22225, 22226, 22227, 22650, 22651, 22652, 22653, 22654, 22655, 22656, 22657, 22658, 22659, 22691, 22692, 22693,
		22694, 22695, 22696, 22697, 22698, 22699, 22700, 22701, 22702, 22703, 22704, 22705, 22706, 22707, 22742, 22743,
		22744, 22745, 22768, 22769, 22770, 22771, 22772, 22773, 22774, 22775, 22776, 22777, 22778, 22779, 22780, 22781,
		22782, 22783, 22784, 22785, 22786, 22787, 22788, 22815, 22818, 22819, 22820, 22821, 22858
	}
	};
	// min and max value of the target adena
	private static final int[][] LUCKPY_ENLARGE_ADENA = {
		{500, 5000}, {1000, 10000}, {2000, 20000}
	};
	// 1000 = 100%
	private static final int HOUR_LIMIT = 1; // in that time player can't spawn another pig by exp ( 0 to disable limit)
	private static final int TRIGGER_CHANCE = 2; // the chance that a Luckpy is spawned after a mob is killed
	private static final int GOOD_FEEDING = 8; // higher value means you need to find the target adena more accurately
	private static final int LUCKPY_EATER = 18664;
	private static final int LUCKPY_NORMAL = 2502;
	private static final int LUCKPY_GOLD = 2503;
	private static final SkillHolder LUCKPY_ENLARGE = new SkillHolder(23325, 1);
	private static final SkillHolder LUCKPY_REDUCE = new SkillHolder(23326, 1);
	private static final int[] GOLD_CHANCE = {20, 35, 50, 50, 50, 50, 50, 50, 50, 50, 50};
	private static final int[][] DROPLIST = {
		// itemId, count, chance
		// normal pig reward
		{8755, 1, 25, 8755, 2, 0}, {5577, 1, 80, 5578, 1, 60, 5579, 1, 40, 5577, 2, 27, 5578, 2, 14, 5579, 2, 0}, {
		9552, 1, 90, 9553, 1, 80, 9554, 1, 70, 9555, 1, 60, 9556, 1, 50, 9557, 1, 40, 9552, 2, 33, 9553, 2, 26, 9554, 2,
		19, 9555, 2, 12, 9556, 2, 6, 9557, 2, 0
	},
		// Gold pig reward
		{14678, 1, 0}, {14679, 1, 0}, {14680, 1, 0}
	};
	private static NpcStringId[] TEXT_RANDOM = {
		NpcStringId.LUCKY_IM_LUCKY_THE_SPIRIT_THAT_LOVES_ADENA, NpcStringId.LUCKY_I_WANT_TO_EAT_ADENA_GIVE_IT_TO_ME,
		NpcStringId.LUCKY_IF_I_EAT_TOO_MUCH_ADENA_MY_WINGS_DISAPPEAR
	};
	private static NpcStringId[] TEXT_EATING = {
		NpcStringId.GRRRR_YUCK, NpcStringId.LUCKY_IT_WASNT_ENOUGH_ADENA_ITS_GOTTA_BE_AT_LEAST_S,
		NpcStringId.YUMMY_THANKS_LUCKY, NpcStringId.LUCKY_THE_ADENA_IS_SO_YUMMY_IM_GETTING_BIGGER,
		NpcStringId.LUCKY_NO_MORE_ADENA_OH_IM_SO_HEAVY,
		NpcStringId.LUCKY_IM_FULL_THANKS_FOR_THE_YUMMY_ADENA_OH_IM_SO_HEAVY
	};
	private static NpcStringId[] TEXT_NOWING = {
		NpcStringId.OH_MY_WINGS_DISAPPEARED_ARE_YOU_GONNA_HIT_ME_IF_YOU_HIT_ME_ILL_THROW_UP_EVERYTHING_THAT_I_ATE,
		NpcStringId.OH_MY_WINGS_ACK_ARE_YOU_GONNA_HIT_ME_SCARY_SCARY_IF_YOU_HIT_ME_SOMETHING_BAD_WILL_HAPPEN
	};
	private Map<L2Npc, LivingLuckpy> _luckpyList = new FastMap<>();

	public TheSpiritOfPigs()
	{
		addStartNpc(LUCKPY_EATER);
		addTalkId(LUCKPY_EATER);
		addFirstTalkId(LUCKPY_EATER);
		for(int[] temp : LUCKPY_TRIGGER_MOB_IDS)
		{
			addKillId(temp);
		}
		addKillId(LUCKPY_NORMAL, LUCKPY_GOLD);
	}

	private static void dropItem(L2Npc mob, L2PcInstance player, int[] droplist)
	{
		int chance = Rnd.get(100);

		for(int i = 0; i < droplist.length / 3; i++)
		{
			if(chance >= droplist[2 + i * 3])
			{
				((L2MonsterInstance) mob).dropItem(player, new ItemHolder(droplist[i * 3], droplist[1 + i * 3]));
				return;
			}
		}
	}

	public static void main(String[] args)
	{
		new TheSpiritOfPigs();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("transform"))
		{
			if(_luckpyList.containsKey(npc))
			{
				_luckpyList.get(npc).eatCount = 10;
			}
		}
		return null;
	}

	@Override
	public String onNpcDie(L2Npc npc, L2Character killer)
	{
		if(killer == null)
		{
			return super.onNpcDie(npc, killer);
		}

		if(_luckpyList.containsKey(npc))
		{
			LivingLuckpy pig = _luckpyList.remove(npc);
			dropItem(npc, (L2PcInstance) killer, DROPLIST[pig.type + (npc.getNpcId() - LUCKPY_NORMAL) * 3]);
		}
		else if(Rnd.get(1000) < TRIGGER_CHANCE)
		{
			for(byte i = 0; i < LUCKPY_TRIGGER_MOB_IDS.length; i++)
			{
				if(ArrayUtils.contains(LUCKPY_TRIGGER_MOB_IDS[i], npc.getNpcId()))
				{
					L2PcInstance player = killer.getActingPlayer();
					if(player != null)
					{
						QuestState st = player.getQuestState(getClass());
						if(st == null)
						{
							Quest q = QuestManager.getInstance().getQuest(getName());
							st = q.newQuestState(player);
							st.set("next_pig", "0");
						}
						if(Long.parseLong(st.get("next_pig")) < System.currentTimeMillis())
						{
							if(HOUR_LIMIT > 0)
							{
								st.setState(STARTED);
								st.set("next_pig", String.valueOf(System.currentTimeMillis() + HOUR_LIMIT * 60000L));
							}
							LivingLuckpy newPig = new LivingLuckpy();
							newPig.type = i;
							newPig.targetAdena = (int) (Rnd.get(LUCKPY_ENLARGE_ADENA[i][0], LUCKPY_ENLARGE_ADENA[i][1]) * Config.RATE_DROP_ITEMS_ID.get(57));
							newPig.spawnTime = System.currentTimeMillis();
							L2Npc newPigNpc = addSpawn(LUCKPY_EATER, npc, true);
							_luckpyList.put(newPigNpc, newPig);
							ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(newPigNpc, 0), 2000);
						}
					}
					break;
				}
			}
		}
		return super.onNpcDie(npc, killer);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(_luckpyList.containsKey(npc))
		{
			return _luckpyList.get(npc).eatCount < 3 ? "18664.htm" : "18664-1.htm";
		}
		npc.getLocationController().delete();
		return null;
	}

	private static class LivingLuckpy
	{
		// 0: lvl52, 1: lvl70, 2: lvl80
		public byte type = -1;
		public int eatCount;
		public byte full;
		public int targetAdena;
		public long spawnTime;
		public Location oldPos;
		public L2ItemInstance food;
	}

	private class LuckpyEaterTask implements Runnable
	{
		private L2Npc _pig;
		private int _status;

		public LuckpyEaterTask(L2Npc pig, int status)
		{
			_pig = pig;
			_status = status;
		}

		@Override
		public void run()
		{
			if(!_luckpyList.containsKey(_pig))
			{
				return;
			}
			if(System.currentTimeMillis() - _luckpyList.get(_pig).spawnTime > 600000)
			{
				// delete pig
				_luckpyList.remove(_pig);
				_pig.getLocationController().delete();
				return;
			}
			LivingLuckpy luckpy = _luckpyList.get(_pig);
			if(_status != 4 && luckpy.eatCount == 10)
			{
				int npcId = Rnd.getChance(GOLD_CHANCE[luckpy.full]) ? LUCKPY_GOLD : LUCKPY_NORMAL;
				L2Npc newPigNpc = addSpawn(npcId, _pig, true);
				_pig.getLocationController().delete();
				_luckpyList.put(newPigNpc, _luckpyList.remove(_pig));
				ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(newPigNpc, 4), 2000);
				return;
			}
			switch(_status)
			{
				case 0:
					if(luckpy.food != null)
					{
						return;
					}
					List<L2Object> foodTargets = WorldManager.getInstance().getVisibleObjects(_pig, 300).stream().filter(object -> object instanceof L2ItemInstance && ((L2ItemInstance) object).getItemId() == PcInventory.ADENA_ID).collect(Collectors.toCollection(FastList::new));
					int minDist = 300000;
					for(L2Object adena : foodTargets)
					{
						int dx = _pig.getX() - adena.getX();
						int dy = _pig.getY() - adena.getY();
						int d = dx * dx + dy * dy;
						if(d < minDist)
						{
							minDist = d;
							luckpy.food = (L2ItemInstance) adena;
						}
					}
					if(minDist == 300000)
					{
						if(Rnd.getChance(20))
						{
							_pig.broadcastPacket(new Say2(_pig.getObjectId(), ChatType.NPC_ALL, _pig.getName(), TEXT_RANDOM[Rnd.get(TEXT_RANDOM.length)]));
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 0), 2000);
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 1), 2000);
					}
					break;
				case 1:
					Location newpos = new Location(luckpy.food.getX(), luckpy.food.getY(), luckpy.food.getZ(), 0);
					luckpy.oldPos = new Location(_pig.getX(), _pig.getY(), _pig.getZ(), _pig.getHeading());
					_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, newpos);
					ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 2), 2000);
					break;
				case 2:
					if(luckpy.food == null || !luckpy.food.isVisible())
					{
						_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, luckpy.oldPos);
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 3), 2000);
					}
					else if(_pig.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
					{
						Long count = luckpy.food.getCount();
						WorldManager.getInstance().removeVisibleObject(luckpy.food, luckpy.food.getLocationController().getWorldRegion());
						WorldManager.getInstance().removeObject(luckpy.food);
						_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
						luckpy.food = null;
						if(Math.abs(luckpy.targetAdena - count) <= LUCKPY_ENLARGE_ADENA[luckpy.type][1] * Config.RATE_DROP_ITEMS_ID.get(57) / (GOOD_FEEDING * 100))
						{
							_pig.broadcastPacket(new Say2(_pig.getObjectId(), ChatType.NPC_ALL, _pig.getName(), TEXT_EATING[Rnd.get(3, 5)]));
							_pig.doCast(LUCKPY_ENLARGE.getSkill());
							luckpy.full++;
						}
						else if(Math.abs(luckpy.targetAdena - count) <= LUCKPY_ENLARGE_ADENA[luckpy.type][1] * Config.RATE_DROP_ITEMS_ID.get(57) / (GOOD_FEEDING * 10))
						{
							_pig.broadcastPacket(new Say2(_pig.getObjectId(), ChatType.NPC_ALL, _pig.getName(), TEXT_EATING[Rnd.get(2, 5)]));
							if(luckpy.full == 0)
							{
								_pig.doCast(LUCKPY_ENLARGE.getSkill());
								luckpy.full++;
							}
						}
						else
						{
							switch(luckpy.full)
							{
								case 2:
								case 1:
									luckpy.full--;
								case 0:
									_pig.doCast(LUCKPY_REDUCE.getSkill());
									break;
								default:
									luckpy.full--;
									break;
							}
							NpcStringId string = TEXT_EATING[Rnd.get(0, 5)];
							if(string.getId() == 1900147)
							{
								// TODO: Добавить параметры как в SystemMessage, возможность заменять S1 т.п. ( Rnd.get(LUCKPY_ENLARGE_ADENA[luckpy.type][0],luckpy.targetAdena) * Config.RATE_DROP_ITEMS_ID.get(57))
							}
							Say2 npcSay = new Say2(_pig.getObjectId(), ChatType.NPC_ALL, _pig.getName(), string);
							_pig.broadcastPacket(npcSay);
						}
						luckpy.eatCount++;
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 0), 2000);
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 2), 2000);
					}
					break;
				case 3:
					if(_pig.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
					{
						_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
						luckpy.food = null;
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 0), 2000);
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 3), 2000);
					}
					break;
				case 4:
					if(!_pig.isDead())
					{
						if(Rnd.getChance(20))
						{
							_pig.broadcastPacket(new Say2(_pig.getObjectId(), ChatType.NPC_ALL, _pig.getName(), TEXT_NOWING[Rnd.get(TEXT_NOWING.length)]));
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 4), 2000);
					}
					break;
			}
		}
	}
}