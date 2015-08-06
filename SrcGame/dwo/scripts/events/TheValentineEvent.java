package dwo.scripts.events;

import dwo.config.events.ConfigEvents;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import org.apache.log4j.Level;

public class TheValentineEvent extends Quest
{
	private static final int _npc = 4301;
	private static final int _recipe = 20191;

	private static final int[][] _spawns = {
		{87792, -142240, -1343, 44000}, {87616, -140688, -1542, 16500}, {114733, -178691, -821, 0},
		{115708, -182362, -1449, 0}, {-44337, -113669, -224, 0}, {-44628, -115409, -240, 22500},
		{-13073, 122801, -3117, 0}, {-13949, 121934, -2988, 32768}, {-14822, 123708, -3117, 8192},
		{-80762, 151118, -3043, 28672}, {-84049, 150176, -3129, 4096}, {-82623, 151666, -3129, 49152},
		{-84516, 242971, -3730, 34000}, {-86003, 243205, -3730, 60000}, {11281, 15652, -4584, 25000},
		{11303, 17732, -4574, 57344}, {47151, 49436, -3059, 32000}, {79806, 55570, -1560, 0},
		{83328, 55824, -1525, 32768}, {80986, 54504, -1525, 32768}, {18178, 145149, -3054, 7400},
		{19208, 144380, -3097, 32768}, {19508, 145775, -3086, 48000}, {17396, 170259, -3507, 30000},
		{83332, 149160, -3405, 49152}, {82277, 148598, -3467, 0}, {81621, 148725, -3467, 32768},
		{81680, 145656, -3533, 32768}, {117498, 76630, -2695, 38000}, {115914, 76449, -2711, 59000},
		{119536, 76988, -2275, 40960}, {147120, 27312, -2192, 40960}, {147920, 25664, -2000, 16384},
		{111776, 221104, -3543, 16384}, {107904, 218096, -3675, 0}, {114920, 220020, -3632, 32768},
		{147888, -58048, -2979, 49000}, {147285, -56461, -2776, 11500}, {44176, -48732, -800, 33000},
		{44294, -47642, -792, 50000}, {-116677, 46824, 360, 34828}
	};

	public TheValentineEvent()
	{
		addStartNpc(_npc);
		addFirstTalkId(_npc);
		addTalkId(_npc);
		for(int[] _spawn : _spawns)
		{
			addSpawn(_npc, _spawn[0], _spawn[1], _spawn[2], _spawn[3], false, 0);
		}
	}

	public static void main(String[] args)
	{
		if(ConfigEvents.EVENT_VALENTINE_ENABLE)
		{
			_log.log(Level.INFO, "[EVENTS] : The Valentine Event Enabled");
			new TheValentineEvent();
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getClass());

		htmltext = event;
		if(event.equalsIgnoreCase("4301-3.htm"))
		{
			if(st.isCompleted())
			{
				htmltext = "4301-4.htm";
			}
			else
			{
				st.giveItems(_recipe, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.setState(QuestStateType.COMPLETED);
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		htmltext = npc.getNpcId() + ".htm";
		return htmltext;
	}
}