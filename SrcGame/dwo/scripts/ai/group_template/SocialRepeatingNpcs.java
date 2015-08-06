package dwo.scripts.ai.group_template;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.12.11
 * Time: 1:38
 *
 * Класс отвечает за "тренирующихся" НПЦ.
 * (Главный НПЦ броадкастит социалки группе)
 */

public class SocialRepeatingNpcs extends Quest
{

	/* ID ПЕРСОНАЖЕЙ */
	// Говорящий Остров
	private static final int TI_Guard_Captain = 33007;
	private static final int TI_Guard_Unit = 33018;

	// Wasteland
	private static final int Wasteland_Guard_Captain = 33434;
	private static final int Wasteland_Guard_Unit = 33437;

	/* ФРАЗЫ ПЕРСОНАЖЕЙ */
	private static final int[] Wasteland_Guard_Captain_Strings = {1811168, 1811169};

	private FastList<L2Npc> _captains = new FastList<>();
	private FastList<L2Npc> _minions = new FastList<>();

	public SocialRepeatingNpcs()
	{
		// 1-ая Группа гвардов возле Шеннона (Говорящий Остров)
		_captains.add(addSpawn(TI_Guard_Captain, -111634, 255351, -1424, 63400, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111533, 255401, -1432, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111534, 255333, -1416, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111536, 255265, -1416, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111443, 255376, -1432, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111444, 255308, -1416, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111457, 255252, -1416, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111374, 255374, -1432, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111376, 255306, -1416, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111378, 255239, -1416, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111285, 255350, -1424, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111309, 255282, -1424, 24288, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -111322, 255226, -1408, 24288, false, 0));

		// 2-ая Группа гвардов возле Ивейна (Говорящий Остров)
		_captains.add(addSpawn(TI_Guard_Captain, -110464, 252064, -1992, 48811, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110336, 253775, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110336, 253900, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110285, 253838, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110279, 253781, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110279, 253905, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110215, 253854, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110212, 253774, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110209, 253921, -1776, 49153, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110342, 253832, -1784, 49153, false, 0));

		// 3-я Группа гвардов возле Подземного Тренировочного Лагеря (Говорящий Остров)
		_captains.add(addSpawn(TI_Guard_Captain, -110263, 253685, -1776, 15416, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110744, 251805, -1968, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110740, 251870, -1968, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110731, 251951, -1960, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110663, 251796, -1976, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110659, 251861, -1976, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110650, 251943, -1968, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110597, 251792, -1976, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110593, 251857, -1976, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110584, 251938, -1976, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110454, 251805, -2000, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110450, 251871, -2000, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110446, 251937, -1992, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110373, 251796, -2000, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110369, 251862, -2000, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110365, 251928, -1992, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110308, 251792, -2016, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110304, 251858, -2016, 13172, false, 0));
		_minions.add(addSpawn(TI_Guard_Unit, -110299, 251924, -2000, 13172, false, 0));

		// 1я Группа Стражей в Пустоши.
		_captains.add(addSpawn(Wasteland_Guard_Captain, -16226, 208095, -3664, 8168, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16114, 208116, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16056, 208224, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16089, 208164, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16179, 208154, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16118, 208256, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16152, 208200, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16188, 208288, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16213, 208234, -3664, 43192, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16244, 208187, -3664, 43192, false, 0));

		// 2я Группа Стражей в Пустоши.
		_captains.add(addSpawn(Wasteland_Guard_Captain, -16316, 209652, -3664, 12772, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16163, 209698, -3664, 41876, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16211, 209726, -3664, 44816, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16273, 209746, -3664, 45472, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16331, 209769, -3664, 46664, false, 0));
		_minions.add(addSpawn(Wasteland_Guard_Unit, -16399, 209787, -3664, 49340, false, 0));

		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new startCaptain(), 15000, 7500);
	}

	public static void main(String[] args)
	{
		new SocialRepeatingNpcs();
	}

	/**
	 * Броадкастим социалку от главных НПЦ группы
	 */
	public class startCaptain implements Runnable
	{
		@Override
		public void run()
		{
			int rndSocial = Rnd.get(3, 7); // TODO: Выбрать нужные ИД социалок, ибо чещуюеся гварды - ни айс xD
			for(L2Npc captain : _captains)
			{
				switch(captain.getNpcId())
				{
					case Wasteland_Guard_Captain:
						captain.broadcastPacket(new NS(captain.getObjectId(), ChatType.NPC_ALL, captain.getNpcId(), Wasteland_Guard_Captain_Strings[Rnd.get(Wasteland_Guard_Captain_Strings.length)]));
						break;
				}
				captain.broadcastPacket(new SocialAction(captain.getObjectId(), rndSocial));
			}
			ThreadPoolManager.getInstance().scheduleAi(new broadcastToMinions(rndSocial), 2000);
		}
	}

	/**
	 * Броадкастим социалку всем НПЦ группы
	 */
	public class broadcastToMinions implements Runnable
	{
		private final int _socialId;

		public broadcastToMinions(int socialId)
		{
			_socialId = socialId;
		}

		@Override
		public void run()
		{
			_minions.stream().filter(minion -> minion != null).forEach(minion -> minion.broadcastPacket(new SocialAction(minion.getObjectId(), _socialId)));
		}
	}
}
