package dwo.scripts.ai.individual;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.util.Set;

/**
 * Dilios AI
 * @author JIV, Sephiroth, Apocalipce
 *
 */
public class GeneralDilios extends Quest
{
	private static final int generalId = 32549;
	private static final int guardId = 32619;
	private static final NpcStringId[] diliosText = {
		NpcStringId.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_WERE_GATHERING_BRAVE_ADVENTURERS_TO_ATTACK_TIATS_MOUNTED_TROOP_THATS_ROOTED_IN_THE_SEED_OF_DESTRUCTION,
		//NpcStringId.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_SEED_OF_DESTRUCTION_IS_CURRENTLY_SECURED_UNDER_THE_FLAG_OF_THE_KEUCEREUS_ALLIANCE,
		//NpcStringId.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_TIATS_MOUNTED_TROOP_IS_CURRENTLY_TRYING_TO_RETAKE_SEED_OF_DESTRUCTION_COMMIT_ALL_THE_AVAILABLE_REINFORCEMENTS_INTO_SEED_OF_DESTRUCTION,
		NpcStringId.MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_BRAVE_ADVENTURERS_WHO_HAVE_CHALLENGED_THE_SEED_OF_INFINITY_ARE_CURRENTLY_INFILTRATING_THE_HALL_OF_EROSION_THROUGH_THE_DEFENSIVELY_WEAK_HALL_OF_SUFFERING,
		//NpcStringId.MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_SWEEPING_THE_SEED_OF_INFINITY_IS_CURRENTLY_COMPLETE_TO_THE_HEART_OF_THE_SEED_EKIMUS_IS_BEING_DIRECTLY_ATTACKED_AND_THE_UNDEAD_REMAINING_IN_THE_HALL_OF_SUFFERING_ARE_BEING_ERADICATED,
		NpcStringId.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_SEED_OF_INFINITY_IS_CURRENTLY_SECURED_UNDER_THE_FLAG_OF_THE_KEUCEREUS_ALLIANCE
		//NpcStringId.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_RESURRECTED_UNDEAD_IN_THE_SEED_OF_INFINITY_ARE_POURING_INTO_THE_HALL_OF_SUFFERING_AND_THE_HALL_OF_EROSION
		//NpcStringId.MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_EKIMUS_IS_ABOUT_TO_BE_REVIVED_BY_THE_RESURRECTED_UNDEAD_IN_SEED_OF_INFINITY_SEND_ALL_REINFORCEMENTS_TO_THE_HEART_AND_THE_HALL_OF_SUFFERING
	};
	private final Set<L2Spawn> _guards;
	private L2Npc _general;

	private GeneralDilios()
	{
		_general = SpawnTable.getInstance().getFirstSpawn(generalId).getLastSpawn();
		_guards = SpawnTable.getInstance().getSpawns(guardId);
		if(_general == null || _guards.isEmpty())
		{
			_log.log(Level.WARN, GeneralDilios.class.getSimpleName() + ": Cannot find NPCs!");
			return;
		}
		startQuestTimer("command_0", 60000, null, null);
	}

	public static void main(String[] args)
	{
		new GeneralDilios();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.startsWith("command_"))
		{
			int value = Integer.parseInt(event.substring(8));
			if(value < 6)
			{
				_general.broadcastPacket(new NS(_general.getObjectId(), ChatType.ALL, generalId, NpcStringId.STABBING_THREE_TIMES));
				startQuestTimer("guard_animation_0", 3400, null, null);
			}
			else
			{
				value = -1;
				_general.broadcastPacket(new NS(_general.getObjectId(), ChatType.SHOUT, generalId, diliosText[Rnd.get(diliosText.length)]));
			}
			startQuestTimer("command_" + (value + 1), 60000, null, null);
		}
		else if(event.startsWith("guard_animation_"))
		{
			int value = Integer.parseInt(event.substring(16));
			for(L2Spawn guard : _guards)
			{
				guard.getLastSpawn().broadcastSocialAction(4);
			}
			if(value < 2)
			{
				startQuestTimer("guard_animation_" + (value + 1), 1500, null, null);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
}