package dwo.gameserver.model.player.base;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.06.12
 * Time: 14:38
 *
 * Классификация сабов: 0 - основной класс, 1 - дуалкласс, 2 - обычный саб
 */

public enum SubClassType
{
	MAIN_CLASS, // TODO: Переделать систему хранения классов (основной класс и подклассы в один набор)
	DUAL_CLASS,
	SUB_CLASS
}