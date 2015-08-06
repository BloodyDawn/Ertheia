
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Eugene Chipachenko
 * Date: 14.09.2014
 * Time: 17:29
 */
public class Spawner
{
  public static final int MOBS = 100;

  private static final L2Territory TERRITORY = new L2Territory( 1 );
  private static final Random RND = new Random();
  private static final List<String> spawnOne = new ArrayList<>();
  private static final List<String> spawnTwo = new ArrayList<>();

  public static void main( String[] args )
  {
    TERRITORY.add( -85550, 157560, -3559, -3559, 0 );
    TERRITORY.add( -86990, 158826, -3744, -3744, 0 );
    TERRITORY.add( -87767, 157432, -3753, -3753, 0 );
    TERRITORY.add( -90923, 154197, -3744, -3744, 0 );
    TERRITORY.add( -89371, 151997, -3641, -3641, 0 );
    TERRITORY.add( -88238, 153606, -3642, -3642, 0 );
    TERRITORY.add( -87028, 154852, -3642, -3642, 0 );
    TERRITORY.add( -86437, 156335, -3624, -3624, 0 );
    TERRITORY.add( -85779, 157175, -3562, -3562, 0 );

    for( int i = 0; i < MOBS; i++ )
    {
      int[] pos = TERRITORY.getRandomPoint();
      int heading = RND.nextInt( 65535 );
      int norm_z = (pos[ 3 ] - pos[ 2 ]) / 2 + pos[ 2 ];

      boolean knight = RND.nextInt( 100 ) < 25;

      if( knight )
      {
        spawnTwo.add( "<spawn " +
                              "heading=\"" + heading + "\" " +
                              "respawn=\"20\" " +
                              "x=\"" + pos[ 0 ] + "\" " +
                              "y=\"" + pos[ 1 ] + "\" " +
                              "z=\"" + norm_z + "\"/>" );
      }
      else
      {
        spawnOne.add( "<spawn " +
                              "heading=\"" + heading + "\" " +
                              "respawn=\"20\" " +
                              "x=\"" + pos[ 0 ] + "\" " +
                              "y=\"" + pos[ 1 ] + "\" " +
                              "z=\"" + norm_z + "\"/>" );
      }
    }

      spawnOne.forEach(System.out::println);

    System.out.println( "----------------------------------" );
      spawnTwo.forEach(System.out::println);
  }
}
