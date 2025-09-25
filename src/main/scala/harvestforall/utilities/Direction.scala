package harvestforall.utilities

/** Direction enumeration for character movement
  */
enum Direction:
  case Up, Down, Left, Right

  /** Get opposite direction
    */
  def opposite: Direction = this match
    case Up    => Down
    case Down  => Up
    case Left  => Right
    case Right => Left

  /** Get direction vector (x, y)
    */
  def vector: (Double, Double) = this match
    case Up    => (0, -1)
    case Down  => (0, 1)
    case Left  => (-1, 0)
    case Right => (1, 0)

end Direction
