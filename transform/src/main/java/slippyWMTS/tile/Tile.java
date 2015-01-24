package slippyWMTS.tile;


public class Tile {
  public int z;
  public int x;
  public int y;

  @Override
  public String toString() {
    return "[" + z + "/" + x + "/" + y + "]";
  }
}
