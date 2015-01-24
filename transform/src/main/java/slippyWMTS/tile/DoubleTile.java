package slippyWMTS.tile;

public class DoubleTile {
  public int z;
  public double x;
  public double y;

  @Override
  public String toString() {
    return "[" + z + "/" + x + "/" + y + "]";
  }
}
