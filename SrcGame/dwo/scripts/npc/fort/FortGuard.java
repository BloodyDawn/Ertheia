package dwo.scripts.npc.fort;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortFacilityType;
import dwo.gameserver.model.world.residence.fort.FortSiegeEngine;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class FortGuard extends Quest
{
	private static final int KE = 9912;

	private static final int[] ARCHER = {
		35789, 35822, 36167, 35853, 36203, 36241, 36279, 35889, 36136, 36029, 35720, 35753, 36065, 36103, 36312, 35922,
		36348, 35958, 35996, 35684, 36386
	};
	private static final int[] ARCHER_DROP = {2, 8, 70};
	private static final int[] ARCHCAP = {
		36028, 35719, 35752, 36064, 36102, 36311, 35921, 36347, 35957, 35995, 35683, 36385, 35788, 35821, 36166, 35852,
		36202, 36240, 36278, 35888, 36135
	};
	private static final int[] ARCHCAP_DROP = {70, 150, 80};
	private static final int[] CGUARD = {
		35673, 35705, 35811, 36156, 36050, 36088, 35842, 36125, 36333, 35943, 35981, 36018, 35742, 36371, 35774, 36226,
		36264, 36301, 36188, 35911, 35874
	};
	private static final int[] CGUARD_DROP = {2, 6, 60};
	private static final int[] DECORATED = {
		35909, 35872, 35671, 35703, 35809, 36154, 36048, 36086, 35840, 36123, 36331, 35979, 36016, 35740, 36369, 35941,
		35772, 36224, 36262, 36299, 36186
	};
	private static final int[] DECORATED_DROP = {2, 8, 75};
	private static final int[] DGSERG = {
		35711, 35814, 36159, 36056, 36094, 35845, 36128, 36339, 35949, 35987, 36021, 35745, 36377, 35780, 36232, 36270,
		36304, 36194, 35914, 35880, 35676
	};
	private static final int[] DGSERG_DROP = {2, 6, 60};
	private static final int[] GENERAL = {36066, 36349, 35959, 35997, 36387, 35790, 36204, 36242, 36280, 35890, 35721};
	private static final int[] GENERAL_DROP = {20, 70, 70};
	private static final int[] GUARD = {
		35944, 36198, 35982, 36236, 36019, 36274, 35743, 36372, 35848, 35884, 35775, 36131, 36227, 36265, 36302, 36189,
		36024, 35715, 35748, 35912, 35875, 35674, 36060, 36098, 35706, 36307, 35812, 36157, 35917, 36051, 36089, 36343,
		35843, 35953, 35991, 35679, 36381, 35784, 36126, 35817, 36162, 36334
	};
	private static final int[] GUARD_DROP = {2, 6, 60};
	private static final int[] GUARDCAP = {
		36058, 36096, 36305, 35915, 36341, 35951, 35989, 35677, 36379, 35782, 35815, 36160, 36196, 36234, 36272, 35846,
		35882, 36129, 36022, 35713, 35746
	};
	private static final int[] GUARDCAP_DROP = {70, 150, 80};
	private static final int[] HEALER = {
		36346, 35956, 35994, 35682, 36384, 35787, 35820, 36165, 35851, 36201, 36239, 36277, 35887, 36134, 36027, 35718,
		35751, 36063, 36101, 36310, 35920
	};
	private static final int[] HEALER_DROP = {2, 8, 80};
	private static final int[] MINISTER = {36205, 36243, 36281, 35891, 35722, 36067, 36350, 35960, 35998, 36388, 35791};
	private static final int[] MINISTER_DROP = {20, 70, 70};
	private static final int[] REBELCOM = {
		36122, 36330, 35978, 36015, 35739, 36368, 35940, 35771, 36223, 36261, 36298, 36185, 35908, 35871, 35670, 35702,
		35808, 36153, 36047, 36085, 35839
	};
	private static final int[] REBELCOM_DROP = {20, 100, 90};
	private static final int[] REBELPRIV = {
		36332, 35980, 36017, 35741, 36370, 35942, 35773, 36225, 36263, 36300, 36187, 35910, 35873, 35672, 35704, 35810,
		36155, 36049, 36087, 35841, 36124
	};
	private static final int[] REBELPRIV_DROP = {2, 6, 70};
	private static final int[] SGOLEM = {
		35781, 36233, 36271, 36195, 35881, 35712, 36057, 36095, 36340, 35950, 35988, 36378
	};
	private static final int[] SGOLEM_DROP = {2, 3, 40};
	private static final int[] SBOX = {35665, 35697, 35803, 35834, 35734, 35766};
	private static final int[] SBOX_DROP = {60, 120, 100};
	private static final int[] SUPPORT = {
		36308, 35918, 36344, 35954, 35992, 35680, 36382, 35785, 35818, 36163, 36199, 36237, 36275, 35849, 35885, 36132,
		36025, 35716, 35749, 36061, 36099
	};
	private static final int[] SUPPORT_DROP = {70, 150, 80};
	private static final int[] WIZARD = {
		36133, 36026, 35717, 35750, 36062, 36100, 36309, 35919, 36345, 35955, 35993, 35681, 36383, 35786, 35819, 36164,
		35850, 36200, 36238, 36276, 35886
	};
	private static final int[] WIZARD_DROP = {2, 8, 70};

	// Скиллы гвардов, зависящие от уровня крепости
	private static final SkillHolder s_castle_power_up_toggle_1 = new SkillHolder(5432, 1);
	private static final SkillHolder s_castle_power_up_toggle_2 = new SkillHolder(5432, 2);

	public FortGuard()
	{
		/*addKillId(CGUARD);
		addKillId(GUARD);
		addKillId(ARCHER);
		addKillId(HEALER);
		addKillId(WIZARD);
		addKillId(SUPPORT);
		addKillId(ARCHCAP);
		addKillId(GUARDCAP);
		addKillId(REBELPRIV);
		addKillId(DECORATED);
		addKillId(REBELCOM);
		addKillId(DGSERG);
		addKillId(GENERAL);
		addKillId(MINISTER);
		addKillId(SGOLEM);
		addKillId(SBOX);*/

		addSpawnId(CGUARD);
		addSpawnId(GUARD);
		addSpawnId(ARCHER);
		addSpawnId(HEALER);
		addSpawnId(WIZARD);
		addSpawnId(SUPPORT);
		addSpawnId(ARCHCAP);
		addSpawnId(GUARDCAP);
		addSpawnId(REBELPRIV);
		addSpawnId(DECORATED);
		addSpawnId(REBELCOM);
		addSpawnId(DGSERG);
		addSpawnId(GENERAL);
		addSpawnId(MINISTER);
		addSpawnId(SGOLEM);
	}

	public static void main(String[] args)
	{
		new FortGuard();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		Fort fort = npc.getFort();
		L2Clan clan = player.getClan();
		if(fort != null && clan != null && fort.getOwnerClan().getClanId() != clan.getClanId())
		{
			FortSiegeEngine siege = fort.getSiege();
			if(siege != null && siege.checkIsAttacker(clan))
			{
				if(ArrayUtils.contains(CGUARD, npcId))
				{
					rewardPlayer(player, npc, CGUARD_DROP);
				}
				else if(ArrayUtils.contains(GUARD, npcId))
				{
					rewardPlayer(player, npc, GUARD_DROP);
				}
				else if(ArrayUtils.contains(ARCHER, npcId))
				{
					rewardPlayer(player, npc, ARCHER_DROP);
				}
				else if(ArrayUtils.contains(HEALER, npcId))
				{
					rewardPlayer(player, npc, HEALER_DROP);
				}
				else if(ArrayUtils.contains(WIZARD, npcId))
				{
					rewardPlayer(player, npc, WIZARD_DROP);
				}
				else if(ArrayUtils.contains(SUPPORT, npcId))
				{
					rewardPlayer(player, npc, SUPPORT_DROP);
				}
				else if(ArrayUtils.contains(ARCHCAP, npcId))
				{
					rewardPlayer(player, npc, ARCHCAP_DROP);
				}
				else if(ArrayUtils.contains(GUARDCAP, npcId))
				{
					rewardPlayer(player, npc, GUARDCAP_DROP);
				}
				else if(ArrayUtils.contains(REBELPRIV, npcId))
				{
					rewardPlayer(player, npc, REBELPRIV_DROP);
				}
				else if(ArrayUtils.contains(DECORATED, npcId))
				{
					rewardPlayer(player, npc, DECORATED_DROP);
				}
				else if(ArrayUtils.contains(REBELCOM, npcId))
				{
					rewardPlayer(player, npc, REBELCOM_DROP);
				}
				else if(ArrayUtils.contains(DGSERG, npcId))
				{
					rewardPlayer(player, npc, DGSERG_DROP);
				}
				else if(ArrayUtils.contains(GENERAL, npcId))
				{
					rewardPlayer(player, npc, GENERAL_DROP);
				}
				else if(ArrayUtils.contains(MINISTER, npcId))
				{
					rewardPlayer(player, npc, MINISTER_DROP);
				}
				else if(ArrayUtils.contains(SGOLEM, npcId))
				{
					rewardPlayer(player, npc, SGOLEM_DROP);
				}
				else if(ArrayUtils.contains(SBOX, npcId))
				{
					rewardPlayer(player, npc, SBOX_DROP);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getFort() != null)
		{
			int guardFortFacilityLevel = npc.getFort().getFacilityLevel(FortFacilityType.FORTRESS_GUARD_POWER_UP);
			if(guardFortFacilityLevel > 0)
			{
				if(guardFortFacilityLevel == 1)
				{
					npc.addSkill(s_castle_power_up_toggle_1.getSkill());
				}
				else if(guardFortFacilityLevel == 2)
				{
					npc.addSkill(s_castle_power_up_toggle_2.getSkill());
				}
			}
		}
		return null;
	}

	private void rewardPlayer(L2PcInstance player, L2Npc npc, int[] drop)
	{
		if(player == null || npc == null)
		{
			return;
		}

		int chance = drop[2] - (player.getLevel() - npc.getLevel());

		if(Rnd.getChance(chance))
		{
			player.addItem(ProcessType.NPC, KE, (long) (Rnd.get(drop[0], drop[1]) * Config.RATE_DROP_ITEMS), npc, true);
			player.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
	}
}