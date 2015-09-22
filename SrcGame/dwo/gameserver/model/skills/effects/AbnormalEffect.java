/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.skills.effects;

import java.util.NoSuchElementException;

/**
 * @author DrHouse
 *         Маски абнормалов согласно GoD Tauti
 */
public enum AbnormalEffect
{
  NULL( "null", 0 ),
  BLEEDING( "bleed", 1 ),
  POISON( "poison", 2 ),
  REDCIRCLE( "redcircle", 3 ),
  ICE( "ice", 4 ),
  WIND( "wind", 5 ),
  FEAR( "fear", 6 ),
  STUN( "stun", 7 ),
  SLEEP( "sleep", 8 ),
  MUTED( "mute", 9 ),
  ROOT( "root", 10 ),
  HOLD_1( "hold1", 11 ),
  HOLD_2( "hold2", 12 ),
  UNKNOWN_13( "unknown13", 13 ),
  BIG_HEAD( "bighead", 14 ),
  FLAME( "flame", 15 ),
  UNKNOWN_16( "lindvior_reflect", 16 ),
  GROW( "grow", 17 ),
  FLY_UP( "flyup", 18 ),
  DANCE_STUNNED( "dancestun", 19 ),
  FIREROOT_STUN( "firerootstun", 20 ),
  STEALTH( "stealth", 21 ),
  IMPRISIONING_1( "imprison1", 22 ),
  IMPRISIONING_2( "imprison2", 23 ),
  MAGIC_CIRCLE( "magiccircle", 24 ),
  ICE2( "ice2", 25 ),
  EARTHQUAKE( "earthquake", 26 ),
  UNKNOWN_27( "unknown27", 27 ),
  INVULNERABLE( "invulnerable", 28 ),
  VITALITY( "vitality", 29 ),
  REAL_TARGET( "realtarget", 30 ),
  DEATH_MARK( "deathmark", 31 ),
  SKULL_FEAR( "skull_fear", 32 ),
  S_INVINCIBLE( "invincible", 33 ), //33
  S_AIR_STUN( "airstun", 34 ), //34
  S_AIR_ROOT( "airroot", 35 ), //35
  S_BAGUETTE_SWORD( "baguettesword", 36 ), // TODO: Тут больше нет абнормала, проверить оастался он ли вообще
  S_YELLOW_AFFRO( "yellowafro", 37 ),
  S_PINK_AFFRO( "pinkafro", 38 ),
  S_BLACK_AFFRO( "blackafro", 39 ),
  UNKNOWN_40( "unknown40", 40 ),
  S_STIGMA_SHILIEN( "stigmashilien", 41 ),
  S_STAKATOROOT( "stakatoroot", 42 ),
  S_FREEZING( "freezing", 43 ), //freya

  S_VESPER_S( "vesper_s", 44 ), //no visual effects
  S_VESPER_C( "vesper_c", 45 ), //no visual effects
  S_VESPER_D( "vesper_d", 46 ), //no visual effects

  HUNTING_BONUS( "hunting_bonus", 47 ),
  ARCANE_SHIELD( "arcane_shield", 48 ),
  S_FLY_UP( "sflyup", 49 ),    // Поднимает таргет в воздух
  CHANGE_BODY( "change_body", 50 ), // Чар стоит но если слать то чар подергивается
  S_KNOCK_DOWN( "sknockdown", 51 ), // Чар лежит
  NAVIT_ADVENT( "nevit_advent", 52 ), // крит клиента
  S_KNOCK_BACK( "sknockback", 53 ),   // History: AEmitter::SetSizeScale <- FNAbnormalStat_NavitAdvent:
  CHANGE_7TH_ANNIVERSARY( "change7th", 54 ),   // шмот ( коричневый )
  ON_SPOT_MOVEMENT( "spot_movement", 55 ),
  S_SUMMON_CELL( "summoncell", 56 ),  //дебаф суммонера он ставит в клетку персонажа(ей) в ней нельзя двигаться, в простонароде "клетка".
  S_STRENGTH_AURA( "aura_of_strength", 57 ),  // светлое Аура Выносливости
  S_ALL_AURA( "aura_off_all", 58 ),  //светлое излучение с пузырьками (если верить оффу то для всех остальных аур)
  S_ANGER_AURA( "aura_of_anger", 59 ),  //красное излучение   Аура Гнева  TODO: Нужно будет уточнять, т.к. в ГоД эффект был другим.

  S_DEBUFF_AURA( "aura_debuff_self", 60 ),

  UNKNOWN_61( "unknown61", 61 ),    // Что то типо взрыва энергии
  S_AIR_BIND( "AirBind", 62 ),  //ураган вокруг перса
  S_SWIM_DEATH_WAIT( "SwimDeathWait", 63 ),  //череп над головой    (красного цвета)
  S_WAITING_DIALOG( "wait_dialog", 68 ), // над головой появялется иконка как буд-то человек думает.

  /*Новые абнормалы*/
  S_NONE2_AURA( "aura_of_none2", 69 ), // оранжевая аура
  S_NONE3_AURA( "aura_of_none3", 70 ), // синяя аура
  S_RED_GLASS( "red_glass", 71 ), //красный глаз над головой

  TALISMAN_POWER1( "talismanpower1", 72 ), //слабое свечение на руке синего цвета
  TALISMAN_POWER2( "talismanpower2", 73 ), //свечение на руке желтого цвета
  TALISMAN_POWER3( "talismanpower3", 74 ), //свечение на руке красного цвета
  TALISMAN_POWER4( "talismanpower4", 75 ), //свечение на руке синего цвета
  TALISMAN_POWER5( "talismanpower5", 76 ), //свечение на руке фиолетового цвета со вспышкой крыльев за спиной

  S_NONE_CLOAK1( "cloak_none1", 77 ), //при этом абнормале на чара одевается плащь и диадема.

  S_ARMOR_LIGHT_LV( "armor_light", 78 ), // сияние и появление шмота ( светлый )    http://bladensoul.ru/scrupload/i/35de65.png
  S_ARMOR_DARK_LV( "armor_dark", 79 ), // сияние и появление шмота ( темный )
  S_LAKSIS( "laksis", 80 ), // сияние и появление шмота ( золотой )
  S_ARMOR_LIGHT2_LV( "armor_light2", 81 ), // сияние и появление шмота ( светлый )
  S_ARMOR_RED_LV( "armor_red", 82 ), // сияние и появление шмота ( красный )

  CHANGE_SWIMSUIT_A( "swimsuit_a", 83 ),  // купальник
  CHANGE_SWIMSUIT_B( "swimsuit_b", 84 ),  // купальник
  S_CHRISTMAS_ARMOR( "hristmas_armor", 85 ),     // Новогодний костюмчик
  S_CHRISTMAS_ARMOR2( "hristmas_armor2", 86 ),   // Новогодний костюмчик
  WIND_BLEND( "wind_blend", 115 ),
  DECEPTIVE_BLINK( "deceptive_blink", 116 ),
  WIND_HIDE( "wind_hide", 117 ),
  SQUALL( "squall", 119 ),
  SPALLATION( "spallation", 127 ); // скил 30515

  private final int _mask;
  private final String _name;

  AbnormalEffect( String name, int mask )
  {
    _name = name;
    _mask = mask;
  }

  public static AbnormalEffect getByName( String name )
  {
    for( AbnormalEffect eff : AbnormalEffect.values() )
    {
      if( eff._name.equals( name ) )
      {
        return eff;
      }
    }

    throw new NoSuchElementException( "AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName() );
  }

  public int getMask()
  {
    return _mask;
  }

  public String getName()
  {
    return _name;
  }
}