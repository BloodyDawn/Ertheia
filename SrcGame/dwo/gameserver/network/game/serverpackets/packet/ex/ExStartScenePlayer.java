package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExStartScenePlayer extends L2GameServerPacket
{
	public static final int SCENE_LINDVIOR = 1;
	public static final int SCENE_EKIMUS_OPENING = 2;
	public static final int SCENE_EKIMUS_SUCCESS = 3;
	public static final int SCENE_EKIMUS_FAIL = 4;
	public static final int SCENE_TIAT_OPENING = 5;
	public static final int SCENE_TIAT_SUCCESS = 6;
	public static final int SCENE_TIAT_FAIL = 7;
	public static final int SCENE_SSQ_SUSPICIOUS_DEATH = 8;
	public static final int SCENE_SSQ_DYING_MASSAGE = 9;
	public static final int SCENE_SSQ_CONTRACT_OF_MAMMON = 10;
	public static final int SCENE_SSQ_RITUAL_OF_PRIEST = 11;
	public static final int SCENE_SSQ_SEALING_EMPEROR_1ST = 12;
	public static final int SCENE_SSQ_SEALING_EMPEROR_2ND = 13;
	public static final int SCENE_SSQ_EMBRYO = 14;
	public static final int SCENE_FREYA_OPENING = 15;
	public static final int SCENE_FREYA_PHASECH_A = 16;
	public static final int SCENE_FREYA_PHASECH_B = 17;
	public static final int SCENE_KEGOR_INTRUSION = 18;
	public static final int SCENE_FREYA_ENDING_A = 19;
	public static final int SCENE_FREYA_ENDING_B = 20;
	public static final int SCENE_FREYA_FORCE_DEFEAT = 21;
	public static final int SCENE_FREYA_DEFEAT = 22;
	public static final int SCENE_HEAVYKNIGHT_SPAWN = 23;
	public static final int SCENE_SSQ2_HOLY_BURIAL_GROUND_OPENING = 24;
	public static final int SCENE_SSQ2_HOLY_BURIAL_GROUND_CLOSING = 25;
	public static final int SCENE_SSQ2_SOLINA_TOMB_OPENING = 26;
	public static final int SCENE_SSQ2_SOLINA_TOMB_CLOSING = 27;
	public static final int SCENE_SSQ2_ELYSS_NARRATION = 28;
	public static final int SCENE_SSQ2_BOSS_OPENING = 29;
	public static final int SCENE_SSQ2_BOSS_CLOSING = 30;
	public static final int SCENE_ISTINA_OPENING = 31;
	public static final int SCENE_ISTINA_ENDING_A = 32;
	public static final int SCENE_ISTINA_ENDING_B = 33;
	public static final int SCENE_ISTINA_BRIDGE = 34;
	public static final int SCENE_OCTABIS_OPENING = 35;
	public static final int SCENE_OCTABIS_PHASECH_A = 36;
	public static final int SCENE_OCTABIS_PHASECH_B = 37;
	public static final int SCENE_OCTABIS_ENDING = 38;
	public static final int SCENE_GD1_PROLOGUE = 42;
	public static final int SCENE_TALKING_ISLAND_BOSS_OPENING = 43;
	public static final int SCENE_TALKING_ISLAND_BOSS_ENDING = 44;
	public static final int SCENE_SORROWFUL_REMEMBRANCE_ENTERING_DWARF_VILLAGE = 45;
	public static final int SCENE_AWAKENING_BOSS_OPENING = 46;
	public static final int SCENE_AWAKENING_BOSS_ENDING_A = 47;
	public static final int SCENE_AWAKENING_BOSS_ENDING_B = 48;
	public static final int SCENE_EARTHWORM_ENDING = 49;
	public static final int SCENE_SPACIA_OPENING = 50;
	public static final int SCENE_SPACIA_A = 51;
	public static final int SCENE_SPACIA_B = 52;
	public static final int SCENE_SPACIA_C = 53;
	public static final int SCENE_SPACIA_ENDING = 54;
	public static final int SCENE_AWAKENING_VIEW = 55;
	public static final int SCENE_SORROWFUL_REMEMBRANCE_TRAJAN = 56;
	public static final int SCENE_SORROWFUL_REMEMBRANCE_ENTERING_DARKELF_VILLAGE = 57;
	public static final int SCENE_SORROWFUL_REMEMBRANCE_SHILLEN = 58;
	public static final int SCENE_SORROWFUL_REMEMBRANCE_EXIT = 59;
	public static final int SCENE_TAUTI_OPENING_B = 69;
	public static final int SCENE_TAUTI_OPENING = 70;
	public static final int SCENE_TAUTI_PHASE = 71;
	public static final int SCENE_TAUTI_ENDING = 72;
	public static final int SCENE_SUB_QUEST = 75;
    public static final int LINDVIOR_ARRIVE = 76;//TODO test
	public static final int SCENE_NOBLE_OPENING = 99;
	public static final int SCENE_NOBLE_ENDING = 100;
	public static final int SCENE_MUSEUM_EXIT_1 = 101;
	public static final int SCENE_MUSEUM_EXIT_2 = 102;
	public static final int SCENE_ENTERING_ESAGIR_RUINS = 103;
	public static final int SCENE_SI_ARKAN_ENTER = 104;
	public static final int SCENE_SI_BARLOG_OPENING = 105;
	public static final int SCENE_SI_BARLOG_STORY = 106;
	public static final int SCENE_SI_ILLUSION_04_QUE = 107;
	public static final int SCENE_SI_ILLUSION_05_QUE = 108;
	public static final int SCENE_BLOODVEIN_OPENING = 109;
	public static final int SCENE_LAND_KSERTH_A = 1000;
	public static final int SCENE_LAND_KSERTH_B = 1001;
	public static final int SCENE_LAND_UNDEAD_A = 1002;
	public static final int SCENE_LAND_DISTRUCTION_A = 1003;
	public static final int SCENE_LAND_ANNIHILATION_A = 1004;
    private int _movieId;

	public ExStartScenePlayer(int id)
	{
		_movieId = id;
	}

	@Override
	public void writeImpl()
	{
		if(getClient().getActiveChar() != null)
		{
			getClient().getActiveChar().setMovieId(_movieId);
		}

		writeD(_movieId);
	}
}
