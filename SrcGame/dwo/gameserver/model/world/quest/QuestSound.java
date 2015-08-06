package dwo.gameserver.model.world.quest;

import dwo.gameserver.network.game.serverpackets.PlaySound;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 31.08.12
 * Time: 11:35
 */

public enum QuestSound
{
	ITEMSOUND_QUEST_ACCEPT(new PlaySound("ItemSound.quest_accept")),
	ITEMSOUND_QUEST_MIDDLE(new PlaySound("ItemSound.quest_middle")),
	ITEMSOUND_QUEST_FINISH(new PlaySound("ItemSound.quest_finish")),
	ITEMSOUND_QUEST_ITEMGET(new PlaySound("ItemSound.quest_itemget")),
	// Newbie Guide tutorial (incl. some quests), Mutated Kaneus quests, Quest 192
	ITEMSOUND_QUEST_TUTORIAL(new PlaySound("ItemSound.quest_tutorial")),
	// Quests 107, 363, 364
	ITEMSOUND_QUEST_GIVEUP(new PlaySound("ItemSound.quest_giveup")),
	// Quests 212, 217, 224, 226, 416
	ITEMSOUND_QUEST_BEFORE_BATTLE(new PlaySound("ItemSound.quest_before_battle")),
	// Quests 211, 258, 266, 330
	ITEMSOUND_QUEST_JACKPOT(new PlaySound("ItemSound.quest_jackpot")),
	// Quests 508, 509 and 510
	ITEMSOUND_QUEST_FANFARE_1(new PlaySound("ItemSound.quest_fanfare_1")),
	// played ONLY after class transfer via Test Server Helpers (ID 31756 and 31757)
	ITEMSOUND_QUEST_FANFARE_2(new PlaySound("ItemSound.quest_fanfare_2")),

	// Quest 114
	ITEMSOUND_ARMOR_WOOD(new PlaySound("ItemSound.armor_wood_3")),
	// Quest 21
	ITEMSOUND_ARMOR_CLOTH(new PlaySound("ItemSound.item_drop_equip_armor_cloth")),
	// Quest 23
	ITEMSOUND_ARMOR_LEATHER(new PlaySound("ItemSound.itemdrop_armor_leather")),
	ITEMSOUND_WEAPON_SPEAR(new PlaySound("ItemSound.itemdrop_weapon_spear")),
	// Quest 24
	AMDSOUND_D_WIND_LOOT_02(new PlaySound("AmdSound.d_wind_loot_02")),
	INTERFACESOUND_CHARSTAT_OPEN_01(new PlaySound("InterfaceSound.charstat_open_01")),

	// Quest 648 and treasure chests
	ITEMSOUND_BROKEN_KEY(new PlaySound("ItemSound2.broken_key")),
	// Quest 184
	ITEMSOUND_SIREN(new PlaySound("ItemSound3.sys_siren")),
	// Quest 648
	ITEMSOUND_ENCHANT_SUCCESS(new PlaySound("ItemSound3.sys_enchant_success")),
	ITEMSOUND_ENCHANT_FAILED(new PlaySound("ItemSound3.sys_enchant_failed")),
	// Best farm mobs
	ITEMSOUND_SOW_SUCCESS(new PlaySound("ItemSound3.sys_sow_success")),

	// Elroki sounds - Quest 111
	ETCSOUND_ELROKI_SOUND_FULL(new PlaySound("EtcSound.elcroki_song_full")),
	ETCSOUND_ELROKI_SOUND_1ST(new PlaySound("EtcSound.elcroki_song_1st")),
	ETCSOUND_ELROKI_SOUND_2ND(new PlaySound("EtcSound.elcroki_song_2nd")),
	ETCSOUND_ELROKI_SOUND_3RD(new PlaySound("EtcSound.elcroki_song_3rd")),

	// Ambient sound
	AMBIENT_SOUND_ED_DRONE(new PlaySound("AmbSound.ed_drone_02")),
	AMBIENT_SOUND_CHIMES(new PlaySound("AmdSound.ed_chimes_05")),
	AMBIENT_SOUND_HORROR(new PlaySound("AmbSound.d_horror_15")),
	AMBIENT_SOUND_QIND_LOOT(new PlaySound("AmdSound.d_wind_loot_02")),
	AMBIENT_SOUND_WINGFLAP(new PlaySound("AmbSound.t_wingflap_04")),
	AMBIENT_SOUND_THUNDER(new PlaySound("AmbSound.thunder_02")),

	// Skill sound
	SKILL_SOUND_HORROR(new PlaySound("SkillSound5.horror_02")),
	SKILL_SOUND_ANTHARAS_FEAR(new PlaySound("SkillSound3.antaras_fear")),

	// Other sound
	OTHER_PAILAKA_BS(new PlaySound("BS08_A")),

	// Interface sound
	INTERFACE_SOUND_CHARSTAT_OPEN(new PlaySound("InterfaceSound.charstat_open_01"));
	private static final Map<String, PlaySound> soundPackets = new HashMap<>();
	private final PlaySound playSound;

	QuestSound(PlaySound playSound)
	{
		this.playSound = playSound;
	}

	/**
	 * Get a {@link PlaySound} packet by its name.
	 * @param soundName : the name of the sound to look for
	 * @return the {@link PlaySound} packet with the specified sound or {@code null} if one was not found
	 */
	public static PlaySound getSound(String soundName)
	{
		if(soundPackets.containsKey(soundName))
		{
			return soundPackets.get(soundName);
		}

		for(QuestSound qs : QuestSound.values())
		{
			if(qs.playSound.getSoundName().equals(soundName))
			{
				soundPackets.put(soundName, qs.playSound); // cache in map to avoid looping repeatedly
				return qs.playSound;
			}
		}

		soundPackets.put(soundName, new PlaySound(soundName));
		return soundPackets.get(soundName);
	}

	/**
	 * @return the name of the sound of this QuestSound object
	 */
	public String getSoundName()
	{
		return playSound.getSoundName();
	}

	/**
	 * @return the {@link PlaySound} packet of this QuestSound object
	 */
	public PlaySound getPacket()
	{
		return playSound;
	}
}