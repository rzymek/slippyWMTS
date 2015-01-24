package slippyWMTS.tile;

public class WmtsTile extends DoubleTile {

  public WmtsTile(double col, double row, int z) {
    this.x = col;
    this.y = row;
    this.z = z;
  }

  public int getX() {
    return (int) x;
  }

  public int getY() {
    return (int) y;
  }

}
