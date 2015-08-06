package dwo.gameserver.network.game;

/**
 * Created with IntelliJ IDEA.
 * User: Eugene Chipachenko
 * Date: 04.10.2014
 * Time: 10:25
 */
public class GameCryptRC4 implements GameCrypt
{
  private RC4 _in;
  private RC4 _out;
  private boolean _isEnabled;

  @Override
  public void setKey( byte[] key )
  {
    _in = new RC4(key);
    _out = new RC4(key);
  }

  @Override
  public void decrypt( final byte[] raw, final int offset, final int size )
  {
    if( !_isEnabled )
    {
      return;
    }

    _in.rc4( raw, offset, size );
  }

  @Override
  public void encrypt( final byte[] raw, final int offset, final int size )
  {
    if( !_isEnabled )
    {
      _isEnabled = true;
      return;
    }
    _out.rc4( raw, offset, size );
  }
}
