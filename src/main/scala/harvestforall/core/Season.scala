package harvestforall.core

/** Season enumeration for seasonal gameplay mechanics
  *
  * Each season affects crop growth, water needs, and farming activities Spent
  * time balancing these modifiers so each season feels different but fair
  */
enum Season:
  case SPRING, SUMMER, AUTUMN, WINTER

  /** Get season description - helps players understand what to expect
    */
  def description: String = this match
    case SPRING => "Spring - Planting season with moderate conditions"
    case SUMMER => "Summer - Growing season with high water needs"
    case AUTUMN => "Autumn - Harvest season with cooler temperatures"
    case WINTER => "Winter - Dormant season with minimal growth"

  /** Get season growth modifier - this is where the seasonal strategy comes in!
    */
  def growthModifier: Double = this match
    case SPRING => 1.2 // Nice bonus for spring planting
    case SUMMER => 1.5 // Peak growing season
    case AUTUMN => 1.1 // Still decent for late crops
    case WINTER => 0.5 // Really slow, but not impossible

  /** Get season water requirement modifier - summer crops are thirsty!
    */
  def waterRequirement: Double = this match
    case SPRING => 1.0
    case SUMMER => 1.8 // Gotta water those crops more in summer heat
    case AUTUMN => 0.8
    case WINTER => 0.3 // Barely need any water in winter

  /** Get next season - simple cycle but important for game progression
    */
  def next: Season = this match
    case SPRING => SUMMER
    case SUMMER => AUTUMN
    case AUTUMN => WINTER
    case WINTER => SPRING

  /** Check if this is a growing season - winter farming is tough but not
    * impossible
    */
  def isGrowingSeason: Boolean = this match
    case SPRING | SUMMER | AUTUMN => true
    case WINTER                   => false

  /** Get season duration in days - short seasons for more dynamic gameplay
    */
  def durationInDays: Int = 5 // Each season lasts 5 game days (5 real days)
