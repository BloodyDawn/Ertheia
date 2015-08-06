package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 15.08.12
 * Time: 12:02
 */

public class PremiumManager extends Quest
{
	private static final int e_premium_manager = 32478;

	// 21275	Купон на Ожерелье Питомца       обменять на Ожерелье Питомца (5 ч.)
	// 21276	Купон на Ожерелье Питомца	Ивент       обменять на Ожерелье Питомца (5 ч.)
	// 13383	Купон на Ожерелье Питомца (5 ч.) - Ивент
	// 13273	Купон на Ожерелье Питомца (5 ч.)
	private static final int[] купон_на_ожерелье_питомца = {21275, 21276, 13383, 13273};

	// 14065	Купон Обмена на Особого Питомца	5 часов
	// 14074	Купон Обмена на Особого Питомца - Ивент	5 часов
	// 21279	Купон Обмена на Особого Питомца
	// 21280    Купон Обмена на Особого Питомца	Ивент
	private static final int[] купон_на_ожерелье_особого_питомца = {14065, 14074, 21279, 21280};

	// 22240	Купон Обмена на Улучшенного Духа Розы (5 ч.)	Ивент
	// 21887	Купон Духа Розы Дня Св. Валентина
	// 20914	Купон Обмена Улучшенного Духа Розы - 5 ч.
	private static final int[] купон_на_ожерелье_улучшенного_питомца = {22240, 21887, 20914};

	public PremiumManager()
	{
		addAskId(e_premium_manager, 21000);
		addAskId(e_premium_manager, 2);
		addAskId(e_premium_manager, 3);
		addAskId(e_premium_manager, 4);
		addAskId(e_premium_manager, 5);
		addAskId(e_premium_manager, 6);
	}

	public static void main(String[] args)
	{
		new PremiumManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == e_premium_manager)
		{
			//e_premium_manager007.htm Простите, но у Вас не хватает купонов для получения питомца. Проверьте, правильно ли Вы выбрали витамин.
			//e_premium_manager008.htm Спасибо!<br1>Приходите еще!

			//       купон_на_ожерелье_питомца
			//       13548	Ожерелье Питомца: Игрушечный Рыцарь		u,Витаминный предмет\\nПозволяет призвать питомца на поле битвы. Нельзя передать или выбросить. Можно положить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//       13549	Ожерелье Питомца: Дух Мага		u,Витаминный предмет\\nПозволяет призвать питомца на поле битвы. Нельзя передать или выбросить. Можно положить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//       13550	Ожерелье Питомца: Сова		u,Витаминный предмет\\nПозволяет призвать питомца на поле битвы. Нельзя передать или выбросить. Можно положить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//       13551	Ожерелье Питомца: Черепаха		u,Витаминный предмет\\nПозволяет призвать питомца на поле битвы. Нельзя передать или выбросить. Можно положить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1

			//      купон_на_ожерелье_особого_питомца
			//      14061	Ожерелье Призыва: Игрушечный Рыцарь		u,Витаминный предмет\\nПозволяет призвать Игрушечного Рыцаря. Нельзя выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      14062	Ожерелье Призыва: Дух Мага		u,Витаминный предмет\\nПозволяет призвать Дух Мага. Нельзя выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      14063	Ожерелье Призыва: Сова		u,Витаминный предмет\\nПозволяет призвать Сову. Нельзя выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      14064	Ожерелье Призыва: Черепаха		u,Витаминный предмет\\nПозволяет призвать Черепаху. Нельзя выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1

			//      20908	Ожерелье Розы Дзеллофа: Ивент	Время призыва: 3 ч.	u,Призывает Духа Розы Дзеллофа. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20909	Ожерелье Розы Хьюма: Ивент	Время призыва: 3 ч.	u,Призывает Духа Розы Хьюма. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20910	Ожерелье Розы Леканга: Ивент	Время призыва: 3 ч.	u,Призывает Духа Розы Леканга. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20911	Ожерелье Розы Лилиат: Ивент	Время призыва: 3 ч.	u,Призывает Духа Розы Лилиат. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20912	Ожерелье Розы Лафам: Ивент	Время призыва: 3 ч.	u,Призывает Духа Розы Лафам. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20913	Ожерелье Розы Мафюм: Ивент	Время призыва: 3 ч.	u,Призывает Духа Розы Мафюм. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1

			//      20915	Улучшенное Ожерелье Розы Дзеллофа: Ивент	Время призыва: 5 ч.	u,Призывает Улучшенного Духа Розы Дзеллофа. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20916	Улучшенное Ожерелье Розы Хьюма: Ивент	Время призыва: 5 ч.	u,Призывает Улучшенного Духа Розы Хьюма. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20917	Улучшенное Ожерелье Розы Леканга: Ивент	Время призыва: 5 ч.	u,Призывает Улучшенного Духа Розы Леканга. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20918	Улучшенное Ожерелье Розы Лилиат: Ивент	Время призыва: 5 ч.	u,Призывает Улучшенного Духа Розы Лилиат. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20919	Улучшенное Ожерелье Розы Лафам: Ивент	Время призыва: 5 ч.	u,Призывает Улучшенного Духа Розы Лафам. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      20920	Улучшенное Ожерелье Розы Мафюм: Ивент	Время призыва: 5 ч.	u,Призывает Улучшенного Духа Розы Мафюм. Нельзя обменять/выбросить/продать, но можно поместить в личное хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1

			//      13017	Ожерелье Питомца: Белая Ласка		u,Витаминный предмет\\nПредмет, с помощью которого каждый раз в определенное время призывается  питомец, использующий вспомогательные умения воинов. Нельзя продать, выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      13018	Ожерелье Питомца: Принцесса Фей		u,Витаминный предмет\\nПредмет, с помощью которого каждый раз в определенное время призывается  питомец, использующий вспомогательные умения магов. Нельзя продать, выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      13019	Ожерелье Питомца: Дикий Зверь Боец		u,Витаминный предмет\\nПредмет, с помощью которого призывается питомец боевого типа. Нельзя продать, выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
			//      13020	Ожерелье Питомца: Лис Шаман		u,Витаминный предмет\\nПредмет, с помощью которого призывается питомец магического типа. Нельзя продать, выбросить, обменять или уничтожить, но можно положить в хранилище.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1

			switch(ask)
			{
				case 2:
					if(reply == 1)
					{
						return "e_premium_manager_q01_02.htm";
					}
					if(reply == 2)
					{
						if(player.getInventory().hasItems(купон_на_ожерелье_питомца))
						{
							player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_питомца);
							player.getInventory().addItem(ProcessType.PRIME_SHOP, 13017, 1, player, npc, true);  // Ожерелье Питомца: Белая Ласка
							return "e_premium_manager_q01_08.htm";
						}
						return "e_premium_manager_q01_07.htm";
					}
				case 3:
					if(reply == 1)
					{
						return "e_premium_manager_q01_03.htm";
					}
					if(reply == 2)
					{
						if(player.getInventory().hasItems(купон_на_ожерелье_питомца))
						{
							player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_питомца);
							player.getInventory().addItem(ProcessType.PRIME_SHOP, 13018, 1, player, npc, true);  // Ожерелье Питомца: Принцесса Фей
							return "e_premium_manager_q01_08.htm";
						}
						return "e_premium_manager_q01_07.htm";
					}
				case 4:
					if(reply == 1)
					{
						return "e_premium_manager_q01_04.htm";
					}
					if(reply == 2)
					{
						if(player.getInventory().hasItems(купон_на_ожерелье_питомца))
						{
							player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_питомца);
							player.getInventory().addItem(ProcessType.PRIME_SHOP, 13019, 1, player, npc, true);  // Ожерелье Питомца: Дикий Зверь Боец
							return "e_premium_manager_q01_08.htm";
						}
						return "e_premium_manager_q01_07.htm";
					}
				case 5:
					if(reply == 1)
					{
						return "e_premium_manager_q01_05.htm";
					}
					if(reply == 2)
					{
						if(player.getInventory().hasItems(купон_на_ожерелье_питомца))
						{
							player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_питомца);
							player.getInventory().addItem(ProcessType.PRIME_SHOP, 13020, 1, player, npc, true);  // Ожерелье Питомца: Лис Шаман
							return "e_premium_manager_q01_08.htm";
						}
						return "e_premium_manager_q01_07.htm";
					}
				case 6:
					if(reply == 1)
					{
						return "e_premium_manager_q01_06.htm";
					}
					if(reply == 2)
					{

						return "e_premium_manager_q01_07.htm";
					}
				case 21000:
					switch(reply)
					{
						// Обменять на Игрушечного Рыцаря
						case 11:
							if(player.getInventory().hasItems(купон_на_ожерелье_особого_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_особого_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 14061, 1, player, npc, true);  // Ожерелье Призыва: Игрушечный Рыцарь
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Обменять на Духа Мага
						case 21:
							if(player.getInventory().hasItems(купон_на_ожерелье_особого_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_особого_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 14062, 1, player, npc, true);  // Ожерелье Призыва: Дух Мага
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Обменять на Сову
						case 31:
							if(player.getInventory().hasItems(купон_на_ожерелье_особого_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_особого_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 14063, 1, player, npc, true);  // Ожерелье Призыва: Сова
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Обменять на черепаху
						case 41:
							if(player.getInventory().hasItems(купон_на_ожерелье_особого_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_особого_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 14064, 1, player, npc, true);  // Ожерелье Призыва: Черепаха
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Я хочу испытать Дзеллофа
						case 51:
							if(player.getInventory().hasItems(купон_на_ожерелье_улучшенного_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_улучшенного_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 20915, 1, player, npc, true);
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Я хочу испытать Хьюма
						case 61:
							if(player.getInventory().hasItems(купон_на_ожерелье_улучшенного_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_улучшенного_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 20916, 1, player, npc, true);
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Я хочу испытать Леканга
						case 71:
							if(player.getInventory().hasItems(купон_на_ожерелье_улучшенного_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_улучшенного_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 20917, 1, player, npc, true);
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Я хочу испытать Лилиат.
						case 81:
							if(player.getInventory().hasItems(купон_на_ожерелье_улучшенного_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_улучшенного_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 20918, 1, player, npc, true);
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Я хочу испытать Лафам.
						case 91:
							if(player.getInventory().hasItems(купон_на_ожерелье_улучшенного_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_улучшенного_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 20919, 1, player, npc, true);
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
						// Я хочу испытать Мафюм.
						case 101:
							if(player.getInventory().hasItems(купон_на_ожерелье_улучшенного_питомца))
							{
								player.destroyOneItemByItemIds(ProcessType.PRIME_SHOP, 1, npc, true, купон_на_ожерелье_улучшенного_питомца);
								player.getInventory().addItem(ProcessType.PRIME_SHOP, 20920, 1, player, npc, true);
								return "e_premium_manager008.htm";
							}
							return "e_premium_manager007.htm";
					}
					break;
			}
		}

		return null;
	}
}