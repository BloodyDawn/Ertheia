package dwo.scripts.npc.instance;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.EventTrigger;
import dwo.scripts.instances.RB_Balok;
import dwo.scripts.instances.RB_Baylor;

import java.util.Calendar;

import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.THURSDAY;
import static java.util.Calendar.TUESDAY;
import static java.util.Calendar.WEDNESDAY;
import static java.util.Calendar.getInstance;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 20.09.12
 * Time: 16:37
 */

	/*
	http://l2central.info/wiki/%D0%A5%D1%80%D1%83%D1%81%D1%82%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9_%D0%9B%D0%B0%D0%B1%D0%B8%D1%80%D0%B8%D0%BD%D1%82
	33522	u,Портал в Хрустальный Лабиринт\0	a,	9C	E8	A9	-1


	<a action="bypass -h menu_select?ask=2423001&reply=1">Войти</a>

	gd_course_enter001a.htm --  Перламутровый Зал      EmeraldSteam
	gd_course_enter000b.htm --  Огненный Коридор
	gd_course_enter000c.htm --  Коралловый Сад         CoralGarden

	gd_course_enter002.htm  --  Портал может использовать только лидер группы.
	gd_course_enter003.htm  --  Нужно быть в группе, чтобы войти.

      npc.broadcastPacket(new EventTrigger(RED_RING, true));

	//	player.sendPacket(new EventTrigger(24230012, false));    // 33523   красный
	//	player.sendPacket(new EventTrigger(24230010, false));    // 33523   голубой
	//	player.sendPacket(new EventTrigger(24230014, false));     // 33522   голубой ( вода )


	*/

public class EntrancePortalToHabitatOfJewels extends Quest
{
	private static final int GD_COURSE_ENTER = 33522;
	private static final int CRYSTAL_PRISON_ENTRANCE_PORTAL = 33523;
	private static final int CRYSTAL_CAVERN_ZONE = 400101;

	public EntrancePortalToHabitatOfJewels()
	{
		addFirstTalkId(GD_COURSE_ENTER);
		addAskId(GD_COURSE_ENTER, 2423001);
		addAskId(CRYSTAL_PRISON_ENTRANCE_PORTAL, 2423001);

		addEnterZoneId(CRYSTAL_CAVERN_ZONE);
	}

	public static void main(String[] args)
	{
		new EntrancePortalToHabitatOfJewels();
	}

	private CrystalCavernsType getType()
	{
		Calendar cal = getInstance();

		if(cal.get(HOUR_OF_DAY) >= 18)
		{
			switch(cal.get(DAY_OF_WEEK))
			{
				case MONDAY:
				case THURSDAY:
				case SUNDAY:
					return CrystalCavernsType.Огненный_Коридор;
				case TUESDAY:
				case FRIDAY:
					return CrystalCavernsType.Перламутровый_Зал;
				case WEDNESDAY:
				case SATURDAY:
					return CrystalCavernsType.Коралловый_Сад;
			}
		}
		else
		{
			switch(cal.get(DAY_OF_WEEK))
			{
				case MONDAY:
				case THURSDAY:
				case SUNDAY:
					return CrystalCavernsType.Перламутровый_Зал;
				case TUESDAY:
				case FRIDAY:
					return CrystalCavernsType.Коралловый_Сад;
				case WEDNESDAY:
				case SATURDAY:
					return CrystalCavernsType.Огненный_Коридор;
			}
		}

		return CrystalCavernsType.Перламутровый_Зал;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 2423001)
		{
			switch(npc.getNpcId())
			{
				case CRYSTAL_PRISON_ENTRANCE_PORTAL:
					if(player.getParty() == null)
					{
						return "gd_boss_enter003.htm";
					}

					switch(reply)
					{
						// Baylor
						case 1:
							if(!player.getParty().getLeader().equals(player))
							{
								return "gd_boss_enter002.htm";
							}
							RB_Baylor.getInstance().enterInstance(player);
							break;
						// Ballok
						case 2:
							if(!player.getParty().getLeader().equals(player))
							{
								return "gd_boss_enter004.htm";
							}
							RB_Balok.getInstance().enterInstance(player);
							break;

					}
				case GD_COURSE_ENTER:
					if(player.getParty() == null)
					{
						return "gd_course_enter003.htm";
					}
					switch(reply)
					{
						case 1:
							if(!player.getParty().getLeader().equals(player))
							{
								return "gd_course_enter002.htm";
							}
							player.sendMessage("В разработке.");
							break;
					}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		switch(getType())
		{
			case Перламутровый_Зал:
				return "gd_course_enter001a.htm";
			case Огненный_Коридор:
				return "gd_course_enter000b.htm";
			case Коралловый_Сад:
				return "gd_course_enter000c.htm";
		}

		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			character.sendPacket(new EventTrigger(24230010, true));
			character.sendPacket(new EventTrigger(24230012, true));
			switch(getType())
			{
				case Перламутровый_Зал:
					character.sendPacket(new EventTrigger(24230018, true));   // зеленый
					break;
				case Огненный_Коридор:
					character.sendPacket(new EventTrigger(24230016, true));   // красный
					break;
				case Коралловый_Сад:
					character.sendPacket(new EventTrigger(24230014, true));   // синий
					break;
			}
		}
		return null;
	}

	private enum CrystalCavernsType
	{
		Перламутровый_Зал,
		Огненный_Коридор,
		Коралловый_Сад
	}
}