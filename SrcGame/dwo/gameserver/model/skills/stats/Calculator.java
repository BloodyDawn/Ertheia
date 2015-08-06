package dwo.gameserver.model.skills.stats;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.funcs.Func;
import javolution.util.FastList;

import java.util.Arrays;

/**
 * Калькулятор служит для управления и динамического изменений характеристик объекта (т.к. : MAX_HP, REGENERATE_HP_RATE...).
 * Калькулятор представляет собой таблицу Функций, где каждая функция представляет собой математическую функцию:
 *
 * FuncPAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()
 *
 * Когда запущен метод calc(), каждая математическая функция считается в зависимости от её приоритета (_order).
 * Стоит заметить, что, чем ниже _order, тем выше приоритет выполнения функции.
 * Результат вычислений сохраняется в значение свойства экземпляра класса Env.
 * Method addFunc and removeFunc permit to add and remove a Func object from a Calculator.<BR><BR>
 * Методы addFunc и removeFunc позволяют добавлять и удалять функции объекта из Calcultator.
 */

public class Calculator
{
	/** Таблица-пустышка для инициализации Calculator */
	private static final Func[] _emptyFuncs = new Func[0];

	/** Таблица функций объекта */
	private Func[] _functions;

	/**
	 * Конструктор Calculator'a (пустым)
	 */
	public Calculator()
	{
		_functions = _emptyFuncs;
	}

	/**
	 * Конструктор Calculator'a
	 * @param c инициализирующее значение
	 */
	public Calculator(Calculator c)
	{
		_functions = c._functions;
	}

	/**
	 * Сравение на идентичность двух Calculator
	 * @param c1 первый Calculator
	 * @param c2 второй Calculator
	 * @return идентичны ли два Calculator'а
	 */
	public static boolean equalsCals(Calculator c1, Calculator c2)
	{
		if(c1 == null || c2 == null)
		{
			return false;
		}

		if(c1.equals(c2))
		{
			return true;
		}

		Func[] funcs1 = c1._functions;
		Func[] funcs2 = c2._functions;

		if(Arrays.equals(funcs1, funcs2))
		{
			return true;
		}

		if(funcs1.length != funcs2.length)
		{
			return false;
		}

		if(funcs1.length == 0)
		{
			return true;
		}

		for(int i = 0; i < funcs1.length; i++)
		{
			if(!funcs1[i].equals(funcs2[i]))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @return количество функций в Calculator
	 */
	public int size()
	{
		return _functions.length;
	}

	/**
	 * @param f добавляемая в Calculator функция
	 */
	public void addFunc(Func f)
	{
		synchronized(this)
		{
			Func[] funcs = _functions;
			Func[] tmp = new Func[funcs.length + 1];

			int order = f.order;
			int i;

			for(i = 0; i < funcs.length && order >= funcs[i].order; i++)
			{
				tmp[i] = funcs[i];
			}

			tmp[i] = f;

			for(; i < funcs.length; i++)
			{
				tmp[i + 1] = funcs[i];
			}

			_functions = tmp;
		}
	}

	/**
	 * @param f удаляемая из Calculator функция
	 */
	public void removeFunc(Func f)
	{
		synchronized(this)
		{
			Func[] funcs = _functions;
			Func[] tmp = new Func[funcs.length - 1];

			int i;

			for(i = 0; i < funcs.length && !f.equals(funcs[i]); i++)
			{
				tmp[i] = funcs[i];
			}

			if(i == funcs.length)
			{
				return;
			}

			for(i++; i < funcs.length; i++)
			{
				tmp[i - 1] = funcs[i];
			}

			_functions = tmp.length == 0 ? _emptyFuncs : tmp;
		}
	}

	/**
	 * Удаление функций у определенного объекта из Calculator.
	 * @param owner объект мира
	 * @return измененные статы персонажа
	 */
	public FastList<Stats> removeOwner(Object owner)
	{
		synchronized(this)
		{
			FastList<Stats> modifiedStats = new FastList<>();
			for(Func func : _functions)
			{
				if(func.funcOwner != null && func.funcOwner.equals(owner))
				{
					modifiedStats.add(func.stat);
					removeFunc(func);
				}
			}
			return modifiedStats;
		}
	}

	/**
	 * Запуск расчета функции в Calculator.
	 * @param env функция
	 */
	public void calc(Env env)
	{
		for(Func func : _functions)
		{
			func.calc(env);
		}
	}

	public FastList<CalculatedStats> getCalculatedStats(L2Character _activeChar, double init)
	{
		FastList<CalculatedStats> modifiedStats = new FastList<>();
		Env env = new Env();
		env.setPlayer(_activeChar);
		env.setValue(init);

		double base = init;
		for(Func func : _functions)
		{
			func.calc(env);
			modifiedStats.add(new CalculatedStats(func, env.getValue() - base));
			base = env.getValue();
		}

		return modifiedStats;
	}

	public class CalculatedStats
	{
		public Func _func;
		public double _vale;

		public CalculatedStats(Func func, double vale)
		{
			_func = func;
			_vale = vale;
		}
	}
}
