package dwo.scripts.ai.group_template;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2FireChestInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class FireChests extends Quest
{
	private static final int[] CHESTS = {
		33699, 33700, 33701, 33702
	};
	// Chance to open chests and get rewards
	private static final int OPEN_CHANCE = 10;
	// Random time for spawn chest after server start/restart (in hours)
	private static final int TIME_MIN = 2;

	// Naive method to spawn random chests. Every respawn time (in hours)
	// all chests will be despawned and spawned new
	// spawn chance is used for every spawn on list so keep it low
	// then is chance to random spawns ;)
	private static final int TIME_MAX = 4;
	// Respawn time
	private static final int RESPAWN_TIME = 4;
	private static final int SPAWN_CHANCE = 20;
	private static final int CHEST_SKILL = 4143;
	private static final int[] ALLOWED_SKILLS = {
		27,        // Unlock
		3155,    // Item Skill : Unlock
		22271    // Maestro key
	};
	/**
	 * Orbis Temple:
	 * 33701, 217077, 48639, -15028, 33465
	 */
	// Id, x,y,z,headin
	private static final int[][] SPAWNLIST = {
		{18265, -45903, 204147, -3719, 36845}, {18265, -46840, 208116, -3479, 365},
		{18265, -44879, 210821, -3359, 36224}, {18265, -51869, 143919, -2888, 43668},
		{18265, -56540, 140357, -2624, 46000}, {18265, -54581, 136542, -2744, 5151},
		{18265, -43858, 117943, -3547, 42031}, {18265, -46213, 110566, -3805, 46258},
		{18265, -53080, 110423, -3568, 13063}, {18266, -96533, 102680, -3492, 31258},
		{18266, -98633, 108321, -3500, 11512}, {18266, -27521, 106384, -3710, 35426},
		{18266, -27792, 111223, -3685, 19669}, {18266, -27282, 117652, -3719, 3992},
		{18266, -19562, 138825, -3890, 18168}, {18266, -23256, 144122, -3866, 25959},
		{18266, -21411, 150031, -3026, 9624}, {18267, -93274, 108878, -3870, 24529},
		{18267, -96668, 113161, -3654, 24288}, {18267, -100435, 108889, -3477, 41466},
		{18267, 14, 178607, -3694, 41057}, {18267, 504, 170547, -3342, 56123}, {18267, 634, 164997, -3383, 44947},
		{18267, 51656, 150916, -2411, 54660}, {18267, 52174, 144991, -2923, 45688},
		{18267, 46651, 141367, -3257, 30227}, {18268, -90593, 114484, -3547, 26311},
		{18268, -95968, 116449, -3330, 31040}, {18268, -59739, 186067, -4807, 45695},
		{18268, -57793, 181483, -4805, 2808}, {18268, -12145, 178496, -4123, 30357},
		{18268, -16213, 175165, -3501, 33268}, {18268, -20435, 177664, -4200, 12730},
		{18269, -21426, 183313, -4039, 17895}, {18269, -27096, 183140, -4341, 42314},
		{18269, -43806, 190623, -3249, 9851}, {18269, -38201, 196087, -3212, 3994},
		{18269, -33657, 198337, -3622, 63186}, {18270, -15605, 188819, -4214, 19820},
		{18270, -20861, 191485, -4117, 21747}, {18270, -20344, 196512, -4203, 18410},
		{18271, 67711, 115108, -3578, 45488}, {18271, 74731, 97979, -3470, 55965}, {18271, 81623, 97903, -3581, 64185},
		{18271, 89331, 99488, -3455, 3641}, {18271, 95918, 101896, -3259, 1321}, {18272, 65130, 35688, -3749, 37260},
		{18272, 12782, 119776, -12082, 33021}, {18272, 11705, 117590, -12085, 16229},
		{18272, 23588, 111193, -9050, 10467}, {18272, 138722, 76225, -3292, 38609},
		{18272, 136162, 73365, -3639, 49589}, {18273, 137100, 10071, -4012, 35779}, {18273, 132759, 7149, -4312, 47159},
		{18273, 139172, 990, -4505, 56261}, {18273, 144872, 86067, -3634, 12715}, {18273, 146161, 89014, -3608, 13610},
		{18273, 144292, 93074, -3548, 24530}, {18274, 83567, 255932, -10355, 65064},
		{18274, 87474, 250279, -9849, 48630}, {18274, 81770, 246157, -9331, 58389},
		{18275, 164087, 18905, -3514, 33895}, {18275, 166683, 15630, -3332, 61426}, {18275, 162849, 8426, -3642, 36309},
		{18276, 151761, 63196, -3376, 14212}, {18276, 156736, 67716, -3496, 7691}, {18276, 162956, 74359, -3015, 5428},
		{18277, 166716, 54476, -4165, 51424}, {18277, 165736, 43381, -3534, 43888},
		{18277, 176794, -23075, -3443, 52984}, {18277, 185279, -25119, -2378, 58656},
		{18278, 115826, 14639, -5102, 55390}, {18278, 115134, 17532, -3611, 17302},
		{18278, 115049, 17334, -2119, 30982}, {18279, 115760, 16659, -639, 63272}, {18279, 113745, 16496, 930, 25242},
		{18279, 172421, -14934, -3050, 35348}, {18280, 115426, 15176, 1955, 48239}, {18280, 80632, -91846, -3122, 689},
		{18280, 90897, -91384, -2822, 60104}, {18280, 49955, -43276, -2626, 7473}, {18280, 48917, -36348, -1433, 17618},
		{18281, 51471, -41425, -2440, 50073}, {18281, 56051, -46875, -3038, 6114}, {18281, 113265, 14329, 2955, 43907},
		{18282, 112350, 17244, 3965, 15066}, {18282, 46651, -40015, -2043, 24165}, {18282, 48365, -35627, -1494, 7443},
		{18283, 112567, 15643, 4985, 16075}, {18283, 147842, -14792, -4382, 36785},
		{18283, 143415, -19326, -3170, 32008}, {18282, 140890, -10843, -4628, 15981},
		{18284, 144790, -87222, -4867, 55174}, {18284, 137241, -87039, -4151, 32023},
		{18284, 128527, -45099, -3286, 46395}, {18284, 118906, -42011, -3080, 26430},
		{18284, 190421, 61721, -6095, 65379}, {18284, 187811, 60710, -7232, 37749},
		{18284, 182730, 60864, -7231, 64892}, {18285, 86814, 79551, -3478, 45074}, {18285, 84506, 73991, -3606, 49827},
		{18285, 87572, 64215, -3692, 57290}, {18285, 81216, 112100, -3648, 54024}, {18285, 85169, 109026, -3158, 58863},
		{18285, 192110, 60885, -6105, 18182}, {18285, 114600, -49702, -2543, 39653},
		{18285, 156475, -78922, -4112, 58115}, {18286, 87201, 60435, -3519, 59275}, {18286, 92785, 63610, -3314, 10046},
		{18286, 144590, 114645, -3710, 5289}, {18286, 153680, 118806, -3795, 59560},
		{18286, 77553, 213362, -3757, 8557}, {18286, 86212, 221717, -3584, 3099}, {18286, 94587, 218811, -3712, 58400},
		{18286, 46142, -91058, -2648, 64251}, {18286, 48309, -84246, -3025, 31963},
		{18286, 51036, -75952, -3413, 64220}, {18286, 87740, -44229, -4125, 26121}, {18286, 80641, -44538, -5126, 45208}
	};

	// TODO: Новые координаты сундуков
	private static final int[][] DROPLIST = {
		// Npc id, dropId, min, max, chance 10000 = 1%
		// 21 lvl
		{18265, 10133, 1, 1, 666700}, // Transformation Scroll: Grail Apostle
		{18265, 10131, 1, 1, 666700}, // Transformation Scroll: Onyx Beast
		{18265, 1538, 1, 1, 333300}, // Blessed Scroll of Escape
		{18265, 1061, 2, 4, 333300}, // Greater Healing Potion
		{18265, 10132, 1, 1, 333300}, // Transformation Scroll: Death Blader
		{18265, 68, 1, 1, 333300}, // Falchion
		{18265, 3936, 1, 1, 222200}, // Blessed Scroll of Resurrection
		{18265, 5593, 1, 6, 222200}, // SP Scroll (Low-Grade)
		{18265, 10269, 1, 1, 222200}, // Defense Juice
		{18265, 10262, 1, 1, 222200}, // Critical Hit Juice
		{18265, 10268, 1, 1, 222200}, // Speed Juice
		{18265, 736, 1, 7, 222200}, // Scroll of Escape
		{18265, 10266, 1, 1, 111100}, // M. Atk. Juice
		{18265, 10264, 1, 1, 111100}, // Casting Spd. Juice
		{18265, 10263, 1, 1, 111100}, // Critical Rate Juice
		{18265, 737, 1, 4, 111100}, // Scroll of Resurrection
		// 24 lvl
		{18266, 737, 1, 4, 666700}, // Scroll of Resurrection
		{18266, 10133, 1, 1, 600000}, // Transformation Scroll: Grail Apostle
		{18266, 1538, 1, 1, 466700}, // Blessed Scroll of Escape
		{18266, 10131, 1, 1, 466700}, // Transformation Scroll: Onyx Beast
		{18266, 10132, 1, 1, 466700}, // Transformation Scroll: Death Blader
		{18266, 5593, 1, 6, 333300}, // SP Scroll (Low-Grade)
		{18266, 10267, 1, 1, 333300}, // Power Juice
		{18266, 736, 1, 7, 333300}, // Scroll of Escape
		{18266, 1061, 2, 4, 266700}, // Greater Healing Potion
		{18266, 68, 1, 1, 266700}, // Falchion
		{18266, 3936, 1, 1, 200000}, // Blessed Scroll of Resurrection
		{18266, 10264, 1, 1, 200000}, // Casting Spd. Juice
		{18266, 5594, 1, 1, 200000}, // SP Scroll (Medium-Grade)
		{18266, 10266, 1, 1, 133300}, // M. Atk. Juice
		{18266, 10260, 1, 1, 133300}, // Alacrity Juice
		{18266, 10263, 1, 1, 133300}, // Critical Rate Juice
		{18266, 10262, 1, 1, 66700}, // Critical Hit Juice
		{18266, 10261, 1, 1, 66700}, // Accuracy Juice
		// 27 lvl
		{18267, 10131, 1, 1, 733300}, // Transformation Scroll: Onyx Beast
		{18267, 10132, 1, 1, 666700}, // Transformation Scroll: Death Blader
		{18267, 10133, 1, 1, 600000}, // Transformation Scroll: Grail Apostle
		{18267, 737, 1, 4, 533300}, // Scroll of Resurrection
		{18267, 1538, 1, 1, 466700}, // Blessed Scroll of Escape
		{18267, 68, 1, 1, 400000}, // Falchion
		{18267, 1061, 2, 4, 333300}, // Greater Healing Potion
		{18267, 10266, 1, 1, 333300}, // M. Atk. Juice
		{18267, 5593, 1, 6, 266700}, // SP Scroll (Low-Grade)
		{18267, 10264, 1, 1, 266700}, // Casting Spd. Juice
		{18267, 10265, 1, 1, 266700}, // Evasion Juice
		{18267, 736, 1, 7, 266700}, // Scroll of Escape
		{18267, 10269, 1, 1, 200000}, // Defense Juice
		{18267, 10268, 1, 1, 200000}, // Speed Juice
		{18267, 10263, 1, 1, 133300}, // Critical Rate Juice
		{18267, 10261, 1, 1, 133300}, // Accuracy Juice
		{18267, 10267, 1, 1, 133300}, // Power Juice
		{18267, 5594, 1, 1, 66700}, // SP Scroll (Medium-Grade)
		// 30 lvl
		{18268, 737, 1, 4, 580600}, // Scroll of Resurrection
		{18268, 10136, 1, 1, 516100}, // Transformation Scroll: Golem Guardian
		{18268, 1538, 1, 1, 451600}, // Blessed Scroll of Escape
		{18268, 10135, 1, 1, 451600}, // Transformation Scroll: Lilim Knight
		{18268, 736, 1, 7, 387100}, // Scroll of Escape
		{18268, 10134, 1, 1, 387100}, // Transformation Scroll: Unicorn
		{18268, 5593, 1, 6, 354800}, // SP Scroll (Low-Grade)
		{18268, 1061, 2, 4, 290300}, // Greater Healing Potion
		{18268, 3936, 1, 1, 225800}, // Blessed Scroll of Resurrection
		{18268, 10262, 1, 1, 225800}, // Critical Hit Juice
		{18268, 10264, 1, 1, 193500}, // Casting Spd. Juice
		{18268, 10261, 1, 1, 193500}, // Accuracy Juice
		{18268, 10265, 1, 1, 193500}, // Evasion Juice
		{18268, 10263, 1, 1, 193500}, // Critical Rate Juice
		{18268, 5594, 1, 1, 193500}, // SP Scroll (Medium-Grade)
		{18268, 10268, 1, 1, 161300}, // Speed Juice
		{18268, 10269, 1, 1, 129000}, // Defense Juice
		{18268, 10266, 1, 1, 129000}, // M. Atk. Juice
		{18268, 10260, 1, 1, 96800}, // Alacrity Juice
		{18268, 10267, 1, 1, 96800}, // Power Juice
		{18268, 69, 1, 1, 96800}, // Bastard Sword
		{18268, 21747, 1, 1, 32300}, // Beginner Adventurer's Treasure Sack
		// 33 lvl
		{18269, 10136, 1, 1, 760000}, // Transformation Scroll: Golem Guardian
		{18269, 10135, 1, 1, 640000}, // Transformation Scroll: Lilim Knight
		{18269, 737, 1, 4, 560000}, // Scroll of Resurrection
		{18269, 10134, 1, 1, 520000}, // Transformation Scroll: Unicorn
		{18269, 1061, 2, 4, 480000}, // Greater Healing Potion
		{18269, 736, 1, 7, 480000}, // Scroll of Escape
		{18269, 1538, 1, 1, 440000}, // Blessed Scroll of Escape
		{18269, 5593, 1, 6, 440000}, // SP Scroll (Low-Grade)
		{18269, 10262, 1, 1, 320000}, // Critical Hit Juice
		{18269, 3936, 1, 1, 200000}, // Blessed Scroll of Resurrection
		{18269, 10265, 1, 1, 200000}, // Evasion Juice
		{18269, 10267, 1, 1, 200000}, // Power Juice
		{18269, 10269, 1, 1, 160000}, // Defense Juice
		{18269, 10266, 1, 1, 160000}, // M. Atk. Juice
		{18269, 10264, 1, 1, 160000}, // Casting Spd. Juice
		{18269, 10268, 1, 1, 160000}, // Speed Juice
		{18269, 69, 1, 1, 160000}, // Bastard Sword
		{18269, 21747, 1, 1, 120000}, // Beginner Adventurer's Treasure Sack
		{18269, 10263, 1, 1, 120000}, // Critical Rate Juice
		{18269, 10260, 1, 1, 96800}, // Alacrity Juice
		// 36 lvl
		{18270, 736, 1, 7, 1000000}, // Scroll of Escape
		{18270, 10134, 1, 1, 1000000}, // Transformation Scroll: Unicorn
		{18270, 3936, 1, 1, 833300}, // Blessed Scroll of Resurrection
		{18270, 1538, 1, 1, 833300}, // Blessed Scroll of Escape
		{18270, 1061, 2, 4, 833300}, // Greater Healing Potion
		{18270, 737, 1, 4, 833300}, // Scroll of Resurrection
		{18270, 10136, 1, 1, 833300}, // Transformation Scroll: Golem Guardian
		{18270, 10135, 1, 1, 500000}, // Transformation Scroll: Lilim Knight
		{18270, 5594, 1, 1, 500000}, // SP Scroll (Medium-Grade)
		{18270, 10269, 1, 1, 333300}, // Defense Juice
		{18270, 10262, 1, 1, 333300}, // Critical Hit Juice
		{18270, 10268, 1, 1, 333300}, // Speed Juice
		{18270, 10267, 1, 1, 333300}, // Power Juice
		{18270, 5593, 1, 6, 166700}, // SP Scroll (Low-Grade)
		{18270, 10264, 1, 1, 166700}, // Casting Spd. Juice
		{18270, 10265, 1, 1, 166700}, // Evasion Juice
		// 39 lvl
		{18271, 5593, 1, 6, 1000000}, // SP Scroll (Low-Grade)
		{18271, 737, 1, 4, 1000000}, // Scroll of Resurrection
		{18271, 10136, 1, 1, 1000000}, // Transformation Scroll: Golem Guardian
		{18271, 10135, 1, 1, 1000000}, // Transformation Scroll: Lilim Knight
		{18271, 1538, 1, 1, 666700}, // Blessed Scroll of Escape
		{18271, 1061, 2, 4, 666700}, // Greater Healing Potion
		{18271, 10260, 1, 1, 666700}, // Alacrity Juice
		{18271, 10261, 1, 1, 666700}, // Accuracy Juice
		{18271, 10265, 1, 1, 666700}, // Evasion Juice
		{18271, 10263, 1, 1, 666700}, // Critical Rate Juice
		{18271, 10134, 1, 1, 666700}, // Transformation Scroll: Unicorn
		{18271, 21747, 1, 1, 333300}, // Beginner Adventurer's Treasure Sack
		{18271, 10269, 1, 1, 333300}, // Defense Juice
		{18271, 10266, 1, 1, 333300}, // M. Atk. Juice
		{18271, 10262, 1, 1, 333300}, // Critical Hit Juice
		{18271, 10267, 1, 1, 333300}, // Power Juice
		{18271, 736, 1, 7, 333300}, // Scroll of Escape
		{18271, 5594, 1, 1, 333300}, // SP Scroll (Medium-Grade)
		// 42 lvl
		{18272, 5593, 1, 9, 689700}, // SP Scroll (Low-Grade)
		{18272, 10137, 1, 1, 620700}, // Transformation Scroll: Inferno Drake
		{18272, 1538, 1, 1, 551700}, // Blessed Scroll of Escape
		{18272, 10269, 1, 1, 551700}, // Defense Juice
		{18272, 10260, 1, 1, 517200}, // Alacrity Juice
		{18272, 10263, 1, 1, 517200}, // Critical Rate Juice
		{18272, 736, 1, 5, 517200}, // Scroll of Escape
		{18272, 8624, 1, 2, 517200}, // Elixir of Life (C-Grade)
		{18272, 10265, 1, 1, 482800}, // Evasion Juice
		{18272, 10268, 1, 1, 482800}, // Speed Juice
		{18272, 5594, 1, 2, 482800}, // SP Scroll (Medium-Grade)
		{18272, 8637, 1, 3, 482800}, // Elixir of CP (B-Grade)
		{18272, 10266, 1, 1, 448300}, // M. Atk. Juice
		{18272, 10264, 1, 1, 448300}, // Casting Spd. Juice
		{18272, 10138, 1, 1, 448300}, // Transformation Scroll: Dragon Bomber
		{18272, 1539, 1, 5, 448300}, // Greater Healing Potion better
		{18272, 8636, 1, 4, 448300}, // Elixir of CP (C-Grade)
		{18272, 10267, 1, 1, 418300}, // Power Juice
		{18272, 10262, 1, 1, 344800}, // Critical Hit Juice
		{18272, 10261, 1, 1, 344800}, // Accuracy Juice
		{18272, 8630, 1, 2, 344800}, // Elixir of Mental Strength (C-Grade)
		{18272, 8625, 1, 2, 310300}, // Elixir of Life (B-Grade)
		{18272, 5578, 1, 1, 275900}, // Green Soul Crystal - Stage 11
		{18272, 3936, 1, 1, 241400}, // Blessed Scroll of Resurrection
		{18272, 1061, 2, 4, 241400}, // Greater Healing Potion
		{18272, 8631, 1, 2, 241400}, // Elixir of Mental Strength (B-Grade)
		{18272, 5577, 1, 1, 172400}, // Red Soul Crystal - Stage 11
		{18272, 5579, 1, 1, 172400}, // Blue Soul Crystal - Stage 12
		{18272, 737, 1, 4, 172400}, // Scroll of Resurrection
		{18272, 70, 1, 1, 137900}, // Claymore
		{18272, 21747, 1, 1, 69000}, // Beginner Adventurer's Treasure Sack
		{18272, 5595, 1, 1, 34500}, // SP Scroll (High Grade)
		// 45 lvl
		{18273, 1538, 1, 1, 861100}, // Blessed Scroll of Escape
		{18273, 5593, 1, 9, 833300}, // SP Scroll (Low-Grade)
		{18273, 1539, 1, 5, 833300}, // Greater Healing Potion better
		{18273, 5594, 1, 2, 777800}, // SP Scroll (Medium-Grade)
		{18273, 10267, 1, 1, 694400}, // Power Juice
		{18273, 10137, 1, 1, 694400}, // Transformation Scroll: Inferno Drake
		{18273, 10266, 1, 1, 666700}, // M. Atk. Juice
		{18273, 10263, 1, 1, 666700}, // Critical Rate Juice
		{18273, 736, 1, 5, 666700}, // Scroll of Escape
		{18273, 8636, 1, 4, 666700}, // Elixir of CP (C-Grade)
		{18273, 10260, 1, 1, 611100}, // Alacrity Juice
		{18273, 10261, 1, 1, 611100}, // Accuracy Juice
		{18273, 10138, 1, 1, 611100}, // Transformation Scroll: Dragon Bomber
		{18273, 10262, 1, 1, 583300}, // Critical Hit Juice
		{18273, 10265, 1, 1, 583300}, // Evasion Juice
		{18273, 10269, 1, 1, 555600}, // Defense Juice
		{18273, 10264, 1, 1, 555600}, // Casting Spd. Juice
		{18273, 10268, 1, 1, 555600}, // Speed Juice
		{18273, 8637, 1, 3, 555600}, // Elixir of CP (B-Grade)
		{18273, 1061, 2, 4, 500000}, // Greater Healing Potion
		{18273, 3936, 1, 1, 416700}, // Blessed Scroll of Resurrection
		{18273, 8625, 1, 2, 416700}, // Elixir of Life (B-Grade)
		{18273, 8630, 1, 2, 416700}, // Elixir of Mental Strength (C-Grade)
		{18273, 8624, 1, 2, 388900}, // Elixir of Life (C-Grade)
		{18273, 8631, 1, 2, 388900}, // Elixir of Mental Strength (B-Grade)
		{18273, 737, 1, 3, 333300}, // Scroll of Resurrection
		{18273, 5577, 1, 1, 138900}, // Red Soul Crystal - Stage 11
		{18273, 5578, 1, 1, 138900}, // Green Soul Crystal - Stage 11
		{18273, 5579, 1, 1, 138900}, // Blue Soul Crystal - Stage 12
		{18273, 70, 1, 1, 111100}, // Claymore
		{18273, 21747, 1, 1, 83300}, // Beginner Adventurer's Treasure Sack
		{18273, 5595, 1, 1, 27800}, // SP Scroll (High Grade)
		// 48 lvl
		{18274, 5593, 1, 9, 963000}, // SP Scroll (Low-Grade)
		{18274, 1538, 1, 1, 888900}, // Blessed Scroll of Escape
		{18274, 1539, 1, 5, 888900}, // Greater Healing Potion better
		{18274, 736, 1, 5, 851900}, // Scroll of Escape
		{18274, 10264, 1, 1, 814800}, // Casting Spd. Juice
		{18274, 5594, 1, 2, 814800}, // SP Scroll (Medium-Grade)
		{18274, 10269, 1, 1, 777800}, // Defense Juice
		{18274, 10267, 1, 1, 777800}, // Power Juice
		{18274, 8637, 1, 3, 777800}, // Elixir of CP (B-Grade)
		{18274, 10266, 1, 1, 740700}, // M. Atk. Juice
		{18274, 10268, 1, 1, 740700}, // Speed Juice
		{18274, 10262, 1, 1, 703700}, // Critical Hit Juice
		{18274, 10261, 1, 1, 703700}, // Accuracy Juice
		{18274, 10260, 1, 1, 629600}, // Alacrity Juice
		{18274, 10263, 1, 1, 629600}, // Critical Rate Juice
		{18274, 21180, 1, 1, 629600}, // Transformation Scroll: Heretic (Event)
		{18274, 8636, 1, 4, 629600}, // Elixir of CP (C-Grade)
		{18274, 10265, 1, 1, 592600}, // Evasion Juice
		{18274, 21181, 1, 1, 555600}, // Transformation Scroll: Veil Master (Event)
		{18274, 8624, 1, 2, 555600}, // Elixir of Life (C-Grade)
		{18274, 1061, 2, 4, 518500}, // Greater Healing Potion
		{18274, 3936, 1, 1, 444400}, // Blessed Scroll of Resurrection
		{18274, 8630, 1, 2, 444400}, // Elixir of Mental Strength (C-Grade)
		{18274, 8625, 1, 2, 407400}, // Elixir of Life (B-Grade)
		{18274, 737, 1, 3, 333300}, // Scroll of Resurrection
		{18274, 5578, 1, 1, 259300}, // Green Soul Crystal - Stage 11
		{18274, 8631, 1, 2, 236900}, // Elixir of Mental Strength (B-Grade)
		{18274, 5577, 1, 1, 222200}, // Red Soul Crystal - Stage 11
		{18274, 5579, 1, 1, 148100}, // Blue Soul Crystal - Stage 11
		{18274, 21747, 1, 1, 111100}, // Beginner Adventurer's Treasure Sack
		{18274, 5595, 1, 1, 74100}, // SP Scroll (High Grade)
		// 51 lvl
		{18275, 1538, 1, 1, 941200}, // Blessed Scroll of Escape
		{18275, 5593, 1, 9, 941200}, // SP Scroll (Low-Grade)
		{18275, 736, 1, 5, 941200}, // Scroll of Escape
		{18275, 1539, 1, 5, 941200}, // Greater Healing Potion better
		{18275, 5594, 1, 2, 882400}, // SP Scroll (Medium-Grade)
		{18275, 10265, 1, 1, 882400}, // Evasion Juice
		{18275, 10266, 1, 1, 764700}, // M. Atk. Juice
		{18275, 10262, 1, 1, 764700}, // Critical Hit Juice
		{18275, 10260, 1, 1, 764700}, // Alacrity Juice
		{18275, 10267, 1, 1, 764700}, // Power Juice
		{18275, 10263, 1, 1, 764700}, // Critical Rate Juice
		{18275, 8637, 1, 3, 764700}, // Elixir of CP (B-Grade)
		{18275, 8624, 1, 2, 764700}, // Elixir of Life (C-Grade)
		{18275, 10269, 1, 1, 705900}, // Defense Juice
		{18275, 10261, 1, 1, 705900}, // Accuracy Juice
		{18275, 21180, 1, 1, 705900}, // Transformation Scroll: Heretic (Event)
		{18275, 8636, 1, 4, 705900}, // Elixir of CP (C-Grade)
		{18275, 1061, 2, 4, 647100}, // Greater Healing Potion
		{18275, 8625, 1, 2, 647100}, // Elixir of Life (B-Grade)
		{18275, 8630, 1, 2, 647100}, // Elixir of Mental Strength (C-Grade)
		{18275, 10264, 1, 1, 588200}, // Casting Spd. Juice
		{18275, 10268, 1, 1, 588200}, // Speed Juice
		{18275, 21181, 1, 1, 529400}, // Transformation Scroll: Veil Master (Event)
		{18275, 8631, 1, 2, 470600}, // Elixir of Mental Strength (B-Grade)
		{18275, 5578, 1, 1, 470600}, // Green Soul Crystal - Stage 11
		{18275, 3936, 1, 1, 352900}, // Blessed Scroll of Resurrection
		{18275, 5579, 1, 1, 294100}, // Blue Soul Crystal - Stage 11
		{18275, 737, 1, 3, 235300}, // Scroll of Resurrection
		{18275, 5577, 1, 1, 235300}, // Red Soul Crystal - Stage 11
		{18275, 5595, 1, 1, 117600}, // SP Scroll (High Grade)
		{18275, 21747, 1, 1, 58800}, // Beginner Adventurer's Treasure Sack
		// 54 lvl
		{18276, 1061, 2, 4, 904800}, // Greater Healing Potion
		{18276, 736, 1, 8, 857100}, // Scroll of Escape
		{18276, 8638, 1, 3, 857100}, // Elixir of CP (A-Grade)
		{18276, 8736, 1, 1, 714300}, // Mid-Grade Life Stone - Level 55
		{18276, 8637, 1, 4, 666700}, // Elixir of CP (B-Grade)
		{18276, 8737, 1, 1, 619000}, // Mid-Grade Life Stone - Level 58
		{18276, 10260, 1, 1, 619000}, // Alacrity Juice
		{18276, 5594, 1, 2, 619000}, // SP Scroll (Medium-Grade)
		{18276, 1538, 1, 2, 571400}, // Blessed Scroll of Escape
		{18276, 8631, 1, 2, 571400}, // Elixir of Mental Strength (B-Grade)
		{18276, 10261, 1, 1, 523800}, // Accuracy Juice
		{18276, 10268, 1, 1, 523800}, // Speed Juice
		{18276, 21182, 1, 1, 523800}, // Transformation Scroll: Saber Tooth Tiger (Event)
		{18276, 8625, 1, 2, 523800}, // Elixir of Life (B-Grade)
		{18276, 8632, 1, 2, 523800}, // Elixir of Mental Strength (A-Grade)
		{18276, 8738, 1, 1, 476200}, // Mid-Grade Life Stone - Level 61
		{18276, 10266, 1, 1, 476200}, // M. Atk. Juice
		{18276, 10262, 1, 1, 476200}, // Critical Hit Juice
		{18276, 10267, 1, 1, 476200}, // Power Juice
		{18276, 737, 1, 3, 476200}, // Scroll of Resurrection
		{18276, 10269, 1, 1, 428600}, // Defense Juice
		{18276, 10263, 1, 1, 428600}, // Critical Rate Juice
		{18276, 8626, 1, 2, 428600}, // Elixir of Life (A-Grade)
		{18276, 3936, 1, 1, 381000}, // Blessed Scroll of Resurrection
		{18276, 10264, 1, 1, 381000}, // Casting Spd. Juice
		{18276, 10265, 1, 1, 381000}, // Evasion Juice
		{18276, 21183, 1, 1, 381000}, // Transformation Scroll: Ol Mahum (Event)
		{18276, 5595, 1, 1, 47600}, // SP Scroll (High Grade)
		{18276, 9649, 1, 1, 47600}, // Transformation Sealbook: Death Blader
		// 57 lvl
		{18277, 736, 1, 8, 1000000}, // Scroll of Escape
		{18277, 8638, 1, 3, 1000000}, // Elixir of CP (A-Grade)
		{18277, 1061, 2, 4, 900000}, // Greater Healing Potion
		{18277, 8637, 1, 4, 800000}, // Elixir of CP (B-Grade)
		{18277, 10266, 1, 1, 700000}, // M. Atk. Juice
		{18277, 10262, 1, 1, 700000}, // Critical Hit Juice
		{18277, 737, 1, 3, 700000}, // Scroll of Resurrection
		{18277, 10264, 1, 1, 600000}, // Casting Spd. Juice
		{18277, 10265, 1, 1, 600000}, // Evasion Juice
		{18277, 10267, 1, 1, 600000}, // Power Juice
		{18277, 10263, 1, 1, 600000}, // Critical Rate Juice
		{18277, 5594, 1, 2, 600000}, // SP Scroll (Medium-Grade)
		{18277, 8632, 1, 2, 600000}, // Elixir of Mental Strength (A-Grade)
		{18277, 8631, 1, 2, 600000}, // Elixir of Mental Strength (B-Grade)
		{18277, 1538, 1, 2, 600000}, // Blessed Scroll of Escape
		{18277, 8736, 1, 1, 500000}, // Mid-Grade Life Stone - Level 55
		{18277, 8737, 1, 1, 500000}, // Mid-Grade Life Stone - Level 58
		{18277, 8738, 1, 1, 500000}, // Mid-Grade Life Stone - Level 61
		{18277, 10261, 1, 1, 500000}, // Accuracy Juice
		{18277, 8625, 1, 2, 500000}, // Elixir of Life (B-Grade)
		{18277, 10269, 1, 1, 400000}, // Defense Juice
		{18277, 21183, 1, 1, 400000}, // Transformation Scroll: Ol Mahum (Event)
		{18277, 8626, 1, 2, 400000}, // Elixir of Life (A-Grade)
		{18277, 10268, 1, 1, 400000}, // Speed Juice
		{18277, 10260, 1, 1, 400000}, // Alacrity Juice
		{18277, 3936, 1, 1, 100000}, // Blessed Scroll of Resurrection
		{18277, 21184, 1, 1, 100000}, // Transformation Scroll: Doll Blader (Event)
		{18277, 5580, 1, 1, 60000}, // Red Soul Crystal - Stage 12
		{18277, 5581, 1, 1, 60000}, // Green Soul Crystal - Stage 12
		{18277, 9649, 1, 1, 30000}, // Transformation Sealbook: Death Blader
		{18277, 9648, 1, 1, 30000}, // Transformation Sealbook: Onyx Beast
		// 60 lvl
		{18278, 8638, 1, 3, 934400}, // Elixir of CP (A-Grade)
		{18278, 1061, 2, 4, 918000}, // Greater Healing Potion
		{18278, 736, 1, 8, 901600}, // Scroll of Escape
		{18278, 8637, 1, 4, 901600}, // Elixir of CP (B-Grade)
		{18278, 8736, 1, 1, 754100}, // Mid-Grade Life Stone - Level 55
		{18278, 21183, 1, 1, 704900}, // Transformation Scroll: Ol Mahum (Event)
		{18278, 5594, 1, 2, 600000}, // SP Scroll (Medium-Grade)
		{18278, 8625, 1, 2, 655700}, // Elixir of Life (B-Grade)
		{18278, 8631, 1, 2, 655700}, // Elixir of Mental Strength (B-Grade)
		{18278, 8737, 1, 1, 639300}, // Mid-Grade Life Stone - Level 58
		{18278, 10267, 1, 1, 623000}, // Power Juice
		{18278, 10262, 1, 1, 606600}, // Critical Hit Juice
		{18278, 10265, 1, 1, 590200}, // Evasion Juice
		{18278, 10269, 1, 1, 573800}, // Defense Juice
		{18278, 8738, 1, 1, 557400}, // Mid-Grade Life Stone - Level 61
		{18278, 21184, 1, 1, 541000}, // Transformation Scroll: Doll Blader (Event)
		{18278, 10266, 1, 1, 541000}, // M. Atk. Juice
		{18278, 10261, 1, 1, 524600}, // Accuracy Juice
		{18278, 10263, 1, 1, 524600}, // Critical Rate Juice
		{18278, 8626, 1, 2, 508200}, // Elixir of Life (A-Grade)
		{18278, 8632, 1, 2, 491800}, // Elixir of Mental Strength (A-Grade)
		{18278, 10264, 1, 1, 475400}, // Casting Spd. Juice
		{18278, 10268, 1, 1, 475400}, // Speed Juice
		{18278, 1538, 1, 2, 442600}, // Blessed Scroll of Escape
		{18278, 10260, 1, 1, 400000}, // Alacrity Juice
		{18278, 737, 1, 3, 426200}, // Scroll of Resurrection
		{18278, 3936, 1, 1, 360700}, // Blessed Scroll of Resurrection
		{18278, 5595, 1, 1, 49200}, // SP Scroll (High Grade)
		{18278, 9648, 1, 1, 32800}, // Transformation Sealbook: Onyx Beast
		{18278, 5581, 1, 1, 16400}, // Green Soul Crystal - Stage 12
		{18278, 5580, 1, 1, 16400}, // Red Soul Crystal - Stage 12
		// 63 lvl
		{18279, 21180, 1, 1, 690900}, // Transformation Scroll: Heretic (Event)
		{18279, 21181, 1, 1, 654500}, // Transformation Scroll: Veil Master (Event)
		{18279, 21182, 1, 1, 600000}, // Transformation Scroll: Saber Tooth Tiger (Event)
		{18279, 1538, 1, 2, 581800}, // Blessed Scroll of Escape
		{18279, 8638, 1, 6, 527300}, // Elixir of CP (A-Grade)
		{18279, 8639, 1, 5, 509100}, // Elixir of CP (S-Grade)
		{18279, 8627, 1, 2, 490900}, // Elixir of Life (S-Grade)
		{18278, 8626, 1, 3, 454500}, // Elixir of Life (A-Grade)
		{18279, 8739, 1, 1, 436400}, // Mid-Grade Life Stone - Level 64
		{18279, 8632, 1, 2, 418200}, // Elixir of Mental Strength (A-Grade)
		{18279, 3936, 1, 1, 363600}, // Blessed Scroll of Resurrection
		{18279, 8740, 1, 1, 363600}, // Mid-Grade Life Stone - Level 67
		{18279, 1540, 1, 4, 345500}, // Quick Healing Potion
		{18279, 8633, 1, 2, 345500}, // Elixir of Mental Strength (S-Grade)
		{18279, 8741, 1, 1, 309100}, // Mid-Grade Life Stone - Level 70
		{18279, 8742, 1, 1, 236400}, // Mid-Grade Life Stone - Level 76
		{18279, 10266, 1, 3, 200000}, // M. Atk. Juice
		{18279, 10260, 1, 3, 200000}, // Alacrity Juice
		{18279, 10265, 1, 3, 181800}, // Evasion Juice
		{18279, 10267, 1, 3, 181800}, // Power Juice
		{18279, 9655, 1, 1, 163600}, // Transformation Sealbook: Dragon Bomber
		{18279, 10269, 1, 3, 163600}, // Defense Juice
		{18279, 10264, 1, 3, 163600}, // Casting Spd. Juice
		{18279, 10263, 1, 3, 163600}, // Critical Rate Juice
		{18279, 10262, 1, 3, 145500}, // Critical Hit Juice
		{18279, 10261, 1, 3, 127300}, // Accuracy Juice
		{18279, 10268, 1, 3, 127300}, // Speed Juice
		{18279, 5595, 1, 1, 90900}, // SP Scroll (High Grade)
		{18279, 9654, 1, 1, 54500}, // Transformation Sealbook: Inferno Drake
		{18279, 9898, 1, 1, 54500}, // SP Scroll: Highest Grade
		{18279, 5581, 1, 1, 35700}, // Green Soul Crystal - Stage 12
		{18279, 5580, 1, 1, 35700}, // Red Soul Crystal - Stage 12
		{18279, 5582, 1, 1, 35700}, // Blue Soul Crystal - Stage 12
		{18279, 21748, 1, 1, 8400}, // Experienced Adventurer's Treasure Sack
		// 66 lvl
		{18280, 21180, 1, 1, 833300}, // Transformation Scroll: Heretic (Event)
		{18280, 21181, 1, 1, 666700}, // Transformation Scroll: Veil Master (Event)
		{18280, 21182, 1, 1, 650000}, // Transformation Scroll: Saber Tooth Tiger (Event)
		{18280, 8638, 1, 6, 650000}, // Elixir of CP (A-Grade)
		{18280, 1538, 1, 2, 550000}, // Blessed Scroll of Escape
		{18280, 8639, 1, 5, 516700}, // Elixir of CP (S-Grade)
		{18280, 3936, 1, 1, 500000}, // Blessed Scroll of Resurrection
		{18280, 8633, 1, 2, 500000}, // Elixir of Mental Strength (S-Grade)
		{18280, 8632, 1, 2, 466700}, // Elixir of Mental Strength (A-Grade)
		{18280, 8627, 1, 2, 450000}, // Elixir of Life (S-Grade)
		{18280, 8739, 1, 1, 433300}, // Mid-Grade Life Stone - Level 64
		{18280, 8740, 1, 1, 416700}, // Mid-Grade Life Stone - Level 67
		{18280, 8626, 1, 3, 383300}, // Elixir of Life (A-Grade)
		{18280, 8741, 1, 1, 350000}, // Mid-Grade Life Stone - Level 70
		{18280, 8742, 1, 1, 350000}, // Mid-Grade Life Stone - Level 76
		{18280, 1540, 1, 4, 333300}, // Quick Healing Potion
		{18280, 10261, 1, 3, 250000}, // Accuracy Juice
		{18280, 10262, 1, 3, 216700}, // Critical Hit Juice
		{18280, 10260, 1, 3, 200000}, // Alacrity Juice
		{18280, 10264, 1, 3, 200000}, // Casting Spd. Juice
		{18280, 10266, 1, 3, 183700}, // M. Atk. Juice
		{18280, 10263, 1, 3, 183300}, // Critical Rate Juice
		{18280, 10268, 1, 3, 133300}, // Speed Juice
		{18280, 10265, 1, 3, 116700}, // Evasion Juice
		{18280, 5595, 1, 1, 66700}, // SP Scroll (High Grade)
		{18280, 9654, 1, 1, 66700}, // Transformation Sealbook: Inferno Drake
		{18280, 9655, 1, 1, 66700}, // Transformation Sealbook: Dragon Bomber
		{18280, 10269, 1, 3, 66700}, // Defense Juice
		{18280, 10267, 1, 3, 66700}, // Power Juice
		{18280, 9898, 1, 1, 66700}, // SP Scroll: Highest Grade
		{18280, 5582, 1, 1, 49200}, // Blue Soul Crystal - Stage 12
		{18280, 5581, 1, 1, 49200}, // Green Soul Crystal - Stage 12
		{18280, 5580, 1, 1, 49200}, // Red Soul Crystal - Stage 12
		{18280, 21748, 1, 1, 8400}, // Experienced Adventurer's Treasure Sack
		// 69 lvl
		{18281, 21184, 1, 1, 608100}, // Transformation Scroll: Doll Blader (Event)
		{18281, 8627, 1, 2, 608100}, // Elixir of Life (S-Grade)
		{18281, 21183, 1, 1, 594600}, // Transformation Scroll: Ol Mahum (Event)
		{18281, 8632, 1, 2, 581100}, // Elixir of Mental Strength (A-Grade)
		{18281, 8638, 1, 6, 567600}, // Elixir of CP (A-Grade)
		{18281, 8639, 1, 5, 554100}, // Elixir of CP (S-Grade)
		{18281, 8739, 1, 1, 527000}, // Mid-Grade Life Stone - Level 64
		{18281, 1538, 1, 2, 513500}, // Blessed Scroll of Escape
		{18281, 8633, 1, 2, 486500}, // Elixir of Mental Strength (S-Grade)
		{18281, 8626, 1, 3, 473000}, // Elixir of Life (A-Grade)
		{18281, 1540, 1, 4, 432400}, // Quick Healing Potion
		{18281, 8740, 1, 1, 405400}, // Mid-Grade Life Stone - Level 67
		{18281, 3936, 1, 1, 391900}, // Blessed Scroll of Resurrection
		{18281, 8741, 1, 1, 310800}, // Mid-Grade Life Stone - Level 70
		{18281, 8742, 1, 1, 297000}, // Mid-Grade Life Stone - Level 76
		{18281, 21185, 1, 1, 297000}, // Transformation Scroll: Zaken (Event)
		{18281, 10269, 1, 3, 243000}, // Defense Juice
		{18281, 10260, 1, 3, 216000}, // Alacrity Juice
		{18281, 10265, 1, 3, 216000}, // Evasion Juice
		{18281, 10264, 1, 3, 202700}, // Casting Spd. Juice
		{18281, 10261, 1, 3, 189200}, // Accuracy Juice
		{18281, 10266, 1, 3, 175700}, // M. Atk. Juice
		{18281, 10262, 1, 3, 175700}, // Critical Hit Juice
		{18281, 10268, 1, 3, 162200}, // Speed Juice
		{18281, 10263, 1, 3, 162200}, // Critical Rate Juice
		{18281, 10267, 1, 3, 148600}, // Power Juice
		{18281, 9655, 1, 1, 96700}, // Transformation Sealbook: Dragon Bomber
		{18281, 9654, 1, 1, 86700}, // Transformation Sealbook: Inferno Drake
		{18281, 9898, 1, 1, 56700}, // SP Scroll: Highest Grade
		{18281, 5595, 1, 1, 46700}, // SP Scroll (High Grade)
		{18281, 5908, 1, 1, 13500}, // Red Soul Crystal - Stage 13
		{18281, 5911, 1, 1, 13500}, // Green Soul Crystal - Stage 13
		{18281, 5914, 1, 1, 13500}, // Blue Soul Crystal - Stage 13
		{18281, 21748, 1, 1, 8400}, // Experienced Adventurer's Treasure Sack
		// 72 lvl
		{18282, 8632, 1, 2, 663000}, // Elixir of Mental Strength (A-Grade)
		{18282, 21184, 1, 1, 618800}, // Transformation Scroll: Doll Blader (Event)
		{18282, 1538, 1, 2, 607700}, // Blessed Scroll of Escape
		{18282, 21183, 1, 1, 607700}, // Transformation Scroll: Ol Mahum (Event)
		{18282, 8639, 1, 5, 607700}, // Elixir of CP (S-Grade)
		{18282, 8627, 1, 2, 574500}, // Elixir of Life (S-Grade)
		{18282, 8739, 1, 1, 530400}, // Mid-Grade Life Stone - Level 64
		{18282, 8638, 1, 6, 530400}, // Elixir of CP (A-Grade)
		{18282, 8633, 1, 2, 530400}, // Elixir of Mental Strength (S-Grade)
		{18282, 3936, 1, 1, 491700}, // Blessed Scroll of Resurrection
		{18282, 1540, 1, 4, 486800}, // Quick Healing Potion
		{18282, 8626, 1, 3, 486800}, // Elixir of Life (A-Grade)
		{18282, 8740, 1, 1, 381200}, // Mid-Grade Life Stone - Level 67
		{18282, 21185, 1, 1, 342500}, // Transformation Scroll: Zaken (Event)
		{18282, 8741, 1, 1, 326000}, // Mid-Grade Life Stone - Level 70
		{18282, 8742, 1, 1, 314900}, // Mid-Grade Life Stone - Level 76
		{18282, 10269, 1, 3, 232000}, // Defense Juice
		{18282, 10268, 1, 3, 226500}, // Speed Juice
		{18282, 10266, 1, 3, 221000}, // M. Atk. Juice
		{18282, 10260, 1, 3, 204400}, // Alacrity Juice
		{18282, 10264, 1, 3, 204400}, // Casting Spd. Juice
		{18282, 10261, 1, 3, 204400}, // Accuracy Juice
		{18282, 10265, 1, 3, 198900}, // Evasion Juice
		{18282, 10263, 1, 3, 193400}, // Critical Rate Juice
		{18282, 10267, 1, 3, 176800}, // Power Juice
		{18282, 10262, 1, 3, 154700}, // Critical Hit Juice
		{18282, 9654, 1, 1, 99400}, // Transformation Sealbook: Inferno Drake
		{18282, 9898, 1, 1, 88400}, // SP Scroll: Highest Grade
		{18282, 5595, 1, 1, 77300}, // SP Scroll (High Grade)
		{18282, 9655, 1, 1, 66300}, // Transformation Sealbook: Dragon Bomber
		{18282, 5911, 1, 1, 11100}, // Green Soul Crystal - Stage 13
		{18282, 5914, 1, 1, 11100}, // Blue Soul Crystal - Stage 13
		{18282, 5908, 1, 1, 11100}, // Red Soul Crystal - Stage 13
		{18282, 21748, 1, 1, 6900}, // Experienced Adventurer's Treasure Sack
		// 75 lvl
		{18283, 21183, 1, 1, 669600}, // Transformation Scroll: Ol Mahum (Event)
		{18283, 1538, 1, 2, 633900}, // Blessed Scroll of Escape
		{18283, 21184, 1, 1, 633900}, // Transformation Scroll: Doll Blader (Event)
		{18283, 8632, 1, 2, 625000}, // Elixir of Mental Strength (A-Grade)
		{18283, 8639, 1, 5, 598200}, // Elixir of CP (S-Grade)
		{18283, 8638, 1, 6, 589300}, // Elixir of CP (A-Grade)
		{18283, 8627, 1, 2, 589300}, // Elixir of Life (S-Grade)
		{18283, 8633, 1, 2, 535700}, // Elixir of Mental Strength (S-Grade)
		{18283, 8626, 1, 3, 526800}, // Elixir of Life (A-Grade)
		{18283, 1540, 1, 4, 491100}, // Quick Healing Potion
		{18283, 8739, 1, 1, 491100}, // Mid-Grade Life Stone - Level 64
		{18283, 3936, 1, 1, 410700}, // Blessed Scroll of Resurrection
		{18283, 8740, 1, 1, 366100}, // Mid-Grade Life Stone - Level 67
		{18283, 8741, 1, 1, 339300}, // Mid-Grade Life Stone - Level 70
		{18283, 8742, 1, 1, 330400}, // Mid-Grade Life Stone - Level 76
		{18283, 21185, 1, 1, 312500}, // Transformation Scroll: Zaken (Event)
		{18283, 10265, 1, 3, 285700}, // Evasion Juice
		{18283, 10264, 1, 3, 276800}, // Casting Spd. Juice
		{18283, 10262, 1, 3, 267900}, // Critical Hit Juice
		{18283, 10261, 1, 3, 241100}, // Accuracy Juice
		{18283, 10269, 1, 3, 214300}, // Defense Juice
		{18283, 10267, 1, 3, 205400}, // Power Juice
		{18283, 10268, 1, 3, 185700}, // Speed Juice
		{18283, 10266, 1, 3, 160700}, // M. Atk. Juice
		{18283, 10263, 1, 3, 142900}, // Critical Rate Juice
		{18283, 10260, 1, 3, 125000}, // Alacrity Juice
		{18283, 5595, 1, 1, 98200}, // SP Scroll (High Grade)
		{18283, 9654, 1, 1, 80400}, // Transformation Sealbook: Inferno Drake
		{18283, 9655, 1, 1, 71400}, // Transformation Sealbook: Dragon Bomber
		{18283, 9898, 1, 1, 71400}, // SP Scroll: Highest Grade
		{18283, 5914, 1, 1, 8900}, // Blue Soul Crystal - Stage 13
		{18283, 5911, 1, 1, 8900}, // Green Soul Crystal - Stage 13
		{18283, 5908, 1, 1, 8900}, // Red Soul Crystal - Stage 13
		{18283, 21748, 1, 1, 8900}, // Experienced Adventurer's Treasure Sack
		// 78 lvl
		{18284, 8639, 1, 4, 1000000}, // Elixir of CP (S-Grade)
		{18284, 8627, 1, 2, 698400}, // Elixir of Life (S-Grade)
		{18284, 1538, 1, 2, 523800}, // Blessed Scroll of Escape
		{18284, 8633, 1, 2, 507900}, // Elixir of Mental Strength (S-Grade)
		{18284, 10269, 1, 3, 460300}, // Defense Juice
		{18284, 10266, 1, 3, 428600}, // M. Atk. Juice
		{18284, 10484, 1, 1, 365100}, // Mid-Grade Life Stone - Level 82
		{18284, 6622, 1, 1, 365100}, // Giant's Codex
		{18284, 10265, 1, 3, 365100}, // Evasion Juice
		{18284, 9574, 1, 1, 349200}, // Mid-Grade Life Stone - Level 80
		{18284, 10267, 1, 3, 349200}, // Power Juice
		{18284, 10485, 1, 1, 333300}, // Mid-Grade Life Stone - Level 84
		{18284, 10260, 1, 3, 317500}, // Alacrity Juice
		{18284, 3936, 1, 1, 317500}, // Blessed Scroll of Resurrection
		{18284, 10268, 1, 3, 285700}, // Speed Juice
		{18284, 10264, 1, 3, 285700}, // Casting Spd. Juice
		{18284, 21188, 1, 1, 269800}, // Transformation Scroll: Gordon (Event)
		{18284, 10263, 1, 3, 254000}, // Critical Rate Juice
		{18284, 10261, 1, 3, 238100}, // Accuracy Juice
		{18284, 1540, 1, 2, 222200}, // Quick Healing Potion
		{18284, 21189, 1, 1, 206300}, // Transformation Scroll: Ranku (Event)
		{18284, 10262, 1, 3, 206300}, // Critical Hit Juice
		{18284, 21186, 1, 1, 206300}, // Transformation Scroll: Anakim (Event)
		{18284, 21187, 1, 1, 174600}, // Transformation Scroll: Venom (Event)
		{18284, 21185, 1, 1, 174600}, // Transformation Scroll: Zaken (Event)
		{18284, 21190, 1, 1, 174600}, // Transformation Scroll: Kechi (Event)
		{18284, 21191, 1, 1, 174600}, // Transformation Scroll: Demon Prince (Event)
		{18284, 9549, 1, 1, 95200}, // Wind Stone
		{18284, 9547, 1, 1, 95200}, // Water Stone
		{18284, 9550, 1, 1, 95200}, // Dark Stone
		{18284, 9548, 1, 1, 95200}, // Earth Stone
		{18284, 5595, 1, 2, 95200}, // SP Scroll (High Grade)
		{18284, 9551, 1, 1, 95200}, // Holy Stone
		{18284, 17185, 1, 1, 31250}, // 1,000,000 SP scroll
		{18284, 9546, 1, 1, 95200}, // Fire Stone
		{18284, 9557, 1, 1, 31250}, // Holy Crystal
		{18284, 9556, 1, 1, 31250}, // Dark Crystal
		{18284, 9555, 1, 1, 31250}, // Wind Crystal
		{18284, 9554, 1, 1, 31250}, // Earth Crystal
		{18284, 9627, 1, 1, 15900}, // Giant's Codex - Mastery
		// 81 lvl
		{18285, 8639, 1, 4, 970300}, // Elixir of CP (S-Grade)
		{18285, 8627, 1, 2, 693100}, // Elixir of Life (S-Grade)
		{18285, 8633, 1, 2, 653500}, // Elixir of Mental Strength (S-Grade)
		{18285, 1538, 1, 2, 564400}, // Blessed Scroll of Escape
		{18285, 9574, 1, 1, 514900}, // Mid-Grade Life Stone - Level 80
		{18285, 10262, 1, 3, 405900}, // Critical Hit Juice
		{18285, 10265, 1, 3, 396000}, // Evasion Juice
		{18285, 10260, 1, 3, 386100}, // Alacrity Juice
		{18285, 3936, 1, 1, 376200}, // Blessed Scroll of Resurrection
		{18285, 10485, 1, 1, 376200}, // Mid-Grade Life Stone - Level 84
		{18285, 10484, 1, 1, 336600}, // Mid-Grade Life Stone - Level 82
		{18285, 6622, 1, 1, 326700}, // Giant's Codex
		{18285, 10269, 1, 3, 326700}, // Defense Juice
		{18285, 10264, 1, 3, 326700}, // Casting Spd. Juice
		{18285, 10263, 1, 3, 326700}, // Critical Rate Juice
		{18285, 10261, 1, 3, 316800}, // Accuracy Juice
		{18285, 10268, 1, 3, 306900}, // Speed Juice
		{18285, 10267, 1, 3, 297000}, // Power Juice
		{18285, 21185, 1, 1, 297000}, // Transformation Scroll: Zaken (Event)
		{18285, 10266, 1, 3, 287100}, // M. Atk. Juice
		{18285, 21187, 1, 1, 227700}, // Transformation Scroll: Venom (Event)
		{18285, 21189, 1, 1, 227700}, // Transformation Scroll: Ranku (Event)
		{18285, 21188, 1, 1, 217800}, // Transformation Scroll: Gordon (Event)
		{18285, 21186, 1, 1, 207900}, // Transformation Scroll: Anakim (Event)
		{18285, 21191, 1, 1, 207900}, // Transformation Scroll: Demon Prince (Event)
		{18285, 21190, 1, 1, 188100}, // Transformation Scroll: Kechi (Event)
		{18285, 1540, 1, 2, 178100}, // Quick Healing Potion
		{18285, 9549, 1, 1, 128700}, // Wind Stone
		{18285, 9551, 1, 1, 128700}, // Holy Stone
		{18285, 9548, 1, 1, 128700}, // Earth Stone
		{18285, 9547, 1, 1, 128700}, // Water Stone
		{18285, 9546, 1, 1, 128700}, // Fire Stone
		{18285, 9550, 1, 1, 128700}, // Dark Stone
		{18285, 5595, 1, 2, 49500}, // SP Scroll (High Grade)
		{18285, 9556, 1, 1, 9900}, // Dark Crystal
		{18285, 9898, 1, 1, 9900}, // SP Scroll: Highest Grade
		{18285, 9555, 1, 1, 9900}, // Wind Crystal
		{18285, 9553, 1, 1, 9900}, // Water Crystal
		{18285, 17185, 1, 1, 8900}, // 1,000,000 SP scroll
		{18285, 21749, 1, 1, 1000}, // Great Adventurer's Treasure Sack
		// 84 lvl
		{18286, 8639, 1, 4, 985100}, // Elixir of CP (S-Grade)
		{18286, 8627, 1, 2, 835800}, // Elixir of Life (S-Grade)
		{18286, 8633, 1, 2, 746300}, // Elixir of Mental Strength (S-Grade)
		{18286, 9574, 1, 1, 537300}, // Mid-Grade Life Stone - Level 80
		{18286, 1538, 1, 2, 447800}, // Blessed Scroll of Escape
		{18286, 10269, 1, 3, 447800}, // Defense Juice
		{18286, 10265, 1, 3, 432800}, // Evasion Juice
		{18286, 10484, 1, 1, 417900}, // Mid-Grade Life Stone - Level 82
		{18286, 10485, 1, 1, 417900}, // Mid-Grade Life Stone - Level 84
		{18286, 6622, 1, 1, 417900}, // Giant's Codex
		{18286, 10268, 1, 3, 417900}, // Speed Juice
		{18286, 10260, 1, 3, 403000}, // Alacrity Juice
		{18286, 10264, 1, 3, 388100}, // Casting Spd. Juice
		{18286, 3936, 1, 1, 373100}, // Blessed Scroll of Resurrection
		{18286, 10262, 1, 3, 343300}, // Critical Hit Juice
		{18286, 10267, 1, 3, 343300}, // Power Juice
		{18286, 10266, 1, 3, 286300}, // M. Atk. Juice
		{18286, 10261, 1, 3, 286300}, // Accuracy Juice
		{18286, 10263, 1, 3, 286300}, // Critical Rate Juice
		{18286, 21185, 1, 1, 286300}, // Transformation Scroll: Zaken (Event)
		{18286, 21190, 1, 1, 286300}, // Transformation Scroll: Kechi (Event)
		{18286, 21187, 1, 1, 268700}, // Transformation Scroll: Venom (Event)
		{18286, 21189, 1, 1, 253700}, // Transformation Scroll: Ranku (Event)
		{18286, 21191, 1, 1, 223900}, // Transformation Scroll: Demon Prince (Event)
		{18286, 1540, 1, 2, 209000}, // Quick Healing Potion
		{18286, 21188, 1, 1, 149300}, // Transformation Scroll: Gordon (Event)
		{18286, 9548, 1, 1, 149300}, // Earth Stone
		{18286, 9546, 1, 1, 149300}, // Fire Stone
		{18286, 21186, 1, 1, 149300}, // Transformation Scroll: Anakim (Event)
		{18286, 9550, 1, 1, 89400}, // Dark Stone
		{18286, 9549, 1, 1, 89400}, // Wind Stone
		{18286, 9551, 1, 1, 89400}, // Holy Stone
		{18286, 9555, 1, 1, 42900}, // Wind Crystal
		{18286, 9554, 1, 1, 42900}, // Earth Crystal
		{18286, 9557, 1, 1, 42900}, // Holy Crystal
		{18286, 9556, 1, 1, 42900}, // Dark Crystal
		{18286, 5595, 1, 2, 19500}, // SP Scroll (High Grade)
		{18286, 9547, 1, 1, 12900}, // Water Stone
		{18286, 9552, 1, 1, 12900}, // Fire Crystal
		{18286, 9898, 1, 1, 7900},  // SP Scroll: Highest Grade
		{18286, 21749, 1, 1, 1000}  // Great Adventurer's Treasure Sack
	};
	private List<L2Npc> npcList = new FastList<>();

	public FireChests()
	{
		addSkillSeeId(CHESTS);
		addSpawnId(CHESTS);
		onSpawnRerun(CHESTS);
		startQuestTimer("start_spawn", 60 * 60 * 1000L * Rnd.get(TIME_MIN, TIME_MAX), null, null);
	}

	public static void main(String[] args)
	{
		// new FireChests();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("start_spawn"))
		{
			spawnChests();
		}
		else if(event.equalsIgnoreCase("despawn"))
		{
			if(npc != null)
			{
				npc.getLocationController().delete();
			}
		}
		else if(event.equalsIgnoreCase("die"))
		{
			if(npc != null)
			{
				npc.doDie(player);
			}
		}
		else if(event.equalsIgnoreCase("spawn_change"))
		{
			spawnChests();
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		int levelDiff = caster.getLevel() >= 78 ? 5 : 6;

		if(npc instanceof L2FireChestInstance && !((L2FireChestInstance) npc).isTriggered())
		{
			if(ArrayUtils.contains(targets, npc) && ArrayUtils.contains(ALLOWED_SKILLS, skill.getId()))
			{
				((L2FireChestInstance) npc).setIsTriggered(true);

				if(npc.getLevel() + levelDiff >= caster.getLevel() && npc.getLevel() - levelDiff <= caster.getLevel())
				{
					if(Rnd.getChance(OPEN_CHANCE))
					{
						makeDrop(npc, caster);
					}
					else
					{
						makeBoom(npc, caster);
					}
				}
				else
				{
					makeBoom(npc, caster);
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(ArrayUtils.contains(CHESTS, npc.getNpcId()))
		{
			npc.setIsNoAttackingBack(true);
		}

		return null;
	}

	private void makeDrop(L2Npc chest, L2PcInstance caster)
	{
		for(int[] data : DROPLIST)
		{
			if(data[0] == chest.getNpcId())
			{
				if(Rnd.get(1000000) < data[4])
				{
					int count = Rnd.get(data[2], data[3]);
					if(count > 0)
					{
						if(Config.AUTO_LOOT && caster.getUseAutoLoot() || caster.isFlying())
						{
							caster.addItem(ProcessType.NPC, data[1], count, chest, true);
						}
						else
						{
							((L2MonsterInstance) chest).dropItem(caster, data[1], count);
						}
					}
				}
			}
		}
		startQuestTimer("die", 500, chest, caster);
	}

	private void makeBoom(L2Npc chest, L2PcInstance caster)
	{
		int skill_level = chest.getLevel() / 10;
		SkillHolder skill = new SkillHolder(CHEST_SKILL, skill_level);
		chest.doCast(skill.getSkill());

		((L2Attackable) chest).addDamageHate(caster, 0, 99);

		chest.setIsNoAttackingBack(false);
		chest.setTarget(caster);
		chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster, null);

		startQuestTimer("despawn", 6000, chest, caster);
	}

	private void spawnChests()
	{
		if(!npcList.isEmpty())
		{
			for(L2Npc npc : npcList)
			{
				npc.getLocationController().delete();
			}
		}
		npcList.clear();
		for(int[] sp : SPAWNLIST)
		{
			if(Rnd.getChance(SPAWN_CHANCE))
			{
				L2Npc chest = addSpawn(sp[0], sp[1], sp[2], sp[3], sp[4], false, 0);
				npcList.add(chest);
			}
		}
		startQuestTimer("spawn_change", RESPAWN_TIME * 3600000L, null, null);
	}
}