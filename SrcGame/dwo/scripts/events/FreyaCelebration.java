package dwo.scripts.events;

import dwo.config.events.ConfigEvents;
import dwo.gameserver.datatables.xml.DynamicSpawnData;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

public class FreyaCelebration extends Quest
{
	private static final int _freya = 13296;
	private static final int _freya_potion = 15440;
	private static final int _freya_gift = 17138;
	private static final int _hours = 20;

	private static final String SPAWN_HOLDER = "freyacelebration";

	private static final int[] _skills = {9150, 9151, 9152, 9153, 9154, 9155, 9156};

	private static final NpcStringId[] FREYA_TEXTS = {
		NpcStringId.EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME,
		NpcStringId.I_JUST_DONT_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME_ARE_HUMANS_EMOTIONS_LIKE_THIS_FEELING,
		NpcStringId.THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME,
		NpcStringId.BUT_I_KIND_OF_MISS_IT_LIKE_I_HAD_FELT_THIS_FEELING_BEFORE,
		NpcStringId.I_AM_ICE_QUEEN_FREYA_THIS_FEELING_AND_EMOTION_ARE_NOTHING_BUT_A_PART_OF_MELISSAA_MEMORIES
	};

	public FreyaCelebration()
	{

		addStartNpc(_freya);
		addFirstTalkId(_freya);
		addTalkId(_freya);
		addSkillSeeId(_freya);

		SpawnsHolder holder = DynamicSpawnData.getInstance().getSpawnsHolder(SPAWN_HOLDER);
		if(holder == null)
		{
			_log.log(Level.WARN, "Spawn holder [" + SPAWN_HOLDER + "] for class: " + getClass().getSimpleName() + " is null!");
			return;
		}
		holder.spawnAll();
	}

	public static void main(String[] args)
	{
		if(ConfigEvents.EVENT_FREYA_CELEBRATION_ENABLE)
		{
			_log.log(Level.INFO, "[EVENTS] : Freya Celebration Event Enabled");
			new FreyaCelebration();
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		Quest q = QuestManager.getInstance().getQuest(getName());
		if(st == null || q == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("give_potion"))
		{
			if(st.hasQuestItems(PcInventory.ADENA_ID))
			{
				long _curr_time = System.currentTimeMillis();
				String value = q.loadGlobalQuestVar(player.getAccountName());
				long _reuse_time = value.isEmpty() ? 0 : Long.parseLong(value);

				if(_curr_time > _reuse_time)
				{
					st.setState(QuestStateType.STARTED);
					st.takeAdena(1);
					st.giveItems(_freya_potion, 1);
					q.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + _hours * 3600000));
				}
				else
				{
					long remainingTime = (_reuse_time - System.currentTimeMillis()) / 1000;
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) (remainingTime % 3600 / 60);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addItemName(_freya_potion);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
				sm.addItemName(PcInventory.ADENA_ID);
				sm.addNumber(1);
				player.sendPacket(sm);
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
			st = newQuestState(player);
		}
		return "13296.htm";
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(caster == null || npc == null)
		{
			return null;
		}

		if(npc.getNpcId() == _freya && ArrayUtils.contains(targets, npc) && ArrayUtils.contains(_skills, skill.getId()))
		{
			if(Rnd.getChance(5))
			{
				Say2 cs = new Say2(npc.getObjectId(), ChatType.ALL, npc.getName(), NpcStringId.DEAR_S1_THINK_OF_THIS_AS_MY_APPRECIATION_FOR_THE_GIFT_TAKE_THIS_WITH_YOU_THERES_NOTHING_STRANGE_ABOUT_IT_ITS_JUST_A_BIT_OF_MY_CAPRICIOUSNESS);
				cs.addStringParameter(caster.getName());

				npc.broadcastPacket(cs);

				caster.addItem(ProcessType.EVENT, _freya_gift, 1, npc, true);
			}
			else
			{
				if(Rnd.getChance(20))
				{
					npc.broadcastPacket(new Say2(npc.getObjectId(), ChatType.ALL, npc.getName(), FREYA_TEXTS[Rnd.get(FREYA_TEXTS.length - 1)]));
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
}