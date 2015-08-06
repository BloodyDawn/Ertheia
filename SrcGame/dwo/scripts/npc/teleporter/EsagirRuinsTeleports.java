package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.12.12
 * Time: 15:29
 */

public class EsagirRuinsTeleports extends Quest
{
	private static final int tel01 = 33180;
	private static final int tel02 = 33181;
	private static final int tel03 = 33182;
	private static final int tel04 = 33183;
	private static final int tel05 = 33184;
	private static final int tel06 = 33185;
	private static final int tel07 = 33186;
	private static final int tel08 = 33187;
	private static final int tel09 = 33188;
	private static final int tel10 = 33189;
	private static final int tel11 = 33190;
	private static final int tel12 = 33191;
	private static final int tel13 = 33192;
	private static final int tel14 = 33193;
	private static final int tel18 = 33197;
	private static final int tel26 = 33205;

	public EsagirRuinsTeleports()
	{
		addAskId(tel01, -3504);

		addAskId(tel02, -3510);

		addAskId(tel03, -3512);
		addAskId(tel03, -3510);

		addAskId(tel04, -3505);

		addAskId(tel06, -3510);

		addAskId(tel07, -3510);
		addAskId(tel07, -3526);

		addAskId(tel08, -3510);
		addAskId(tel08, -3522);

		addAskId(tel09, -3510);
		addAskId(tel09, -3524);

		addAskId(tel10, -3510);
		addAskId(tel10, -3516);

		addAskId(tel11, -3510);
		addAskId(tel11, -3514);

		addAskId(tel12, -3510);
		addAskId(tel12, -3520);

		addAskId(tel13, -3510);
		addAskId(tel13, -3518);

		addAskId(tel14, -3502);

		addAskId(tel18, -3528);
		addAskId(tel18, -3509);
		addAskId(tel18, -3508);
		addAskId(tel18, -3507);

		addAskId(tel26, -3528);
	}

	public static void main(String[] args)
	{
		new EsagirRuinsTeleports();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -3502:
				if(reply == 1) // Вернуться в город (Тренировочный зал)
				{
					player.teleToLocation(-110380, 252472, -1992);
					player.getInstanceController().setInstanceId(0);
				}
				break;
			case -3504:
				if(reply == 1) // Войти в Руины Эсагира
				{
					player.teleToLocation(-114675, 230171, -1648);
				}
				break;
			case -3505:
				if(reply == 1) // Войти в Руины Эсагира
				{
					player.teleToLocation(-114675, 230171, -1648);
				}
				break;
			case -3507:
				if(reply == 1) // Выйти через Восточный выход
				{
					player.teleToLocation(-109294, 237397, -2928);
				}
				break;
			case -3508:
				if(reply == 1) // Переместиться в 1-ю Зону Исследования
				{
					player.teleToLocation(-115005, 237383, -3088);
				}
				break;
			case -3509:
				if(reply == 1) // Выйти через Западный выход
				{
					player.teleToLocation(-122189, 241009, -2328);
				}
				break;
			case -3510:
				if(reply == 1) // Вернуться на Наблюдательную Вышку Эсагира
				{
					player.teleToLocation(-114675, 230171, -1648);
				}
				break;
			case -3512:
				if(reply == 1) // Переместиться в 2-ю Зону Исследования
				{
					player.teleToLocation(-118350, 233992, -2904);
				}
				break;
			case -3514:
				if(reply == 1) // Переместиться в 1-ю Зону Исследования
				{
					player.teleToLocation(-115005, 237383, -3088);
				}
				break;
			case -3516:
				if(reply == 1) // Переместиться в 3-ю Зону Исследования
				{
					player.teleToLocation(-116303, 239062, -2736);
				}
				break;
			case -3518:
				if(reply == 1) // Переместиться в 2-ю Зону Исследования
				{
					player.teleToLocation(-118350, 233992, -2904);
				}
				break;
			case -3520:
				if(reply == 1) // Переместиться в 4-ю Зону Исследования
				{
					player.teleToLocation(-112382, 238710, -2904);
				}
				break;
			case -3522:
				if(reply == 1) // Переместиться в 3-ю Зону Исследования
				{
					player.teleToLocation(-116303, 239062, -2736);
				}
				break;
			case -3524:
				if(reply == 1) // Переместиться в 5-ю Зону Исследования
				{
					player.teleToLocation(-110980, 233774, -3200);
				}
				break;
			case -3526:
				if(reply == 1) // Переместиться в 4-ю Зону Исследования
				{
					player.teleToLocation(-112382, 238710, -2904);
				}
				break;
			case -3528:
				if(reply == 1) // Вернуться в город
				{
					player.teleToLocation(-114413, 252159, -1592);
				}
				break;
		}
		return null;
	}
}