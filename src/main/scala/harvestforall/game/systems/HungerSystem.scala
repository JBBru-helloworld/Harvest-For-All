package harvestforall.game.systems

import harvestforall.game.ui.PlayerLifeSystem
import scala.concurrent.duration.*

/** Hunger system that decreases player health over time Uses modern Scala 3
  * features: enums, extension methods, given instances
  */
class HungerSystem(using playerLifeSystem: PlayerLifeSystem):

  // Hunger configuration using enums
  enum HungerLevel(val displayName: String, val threshold: Double):
    case WellFed extends HungerLevel("Well Fed", 80.0)
    case Satisfied extends HungerLevel("Satisfied", 60.0)
    case Hungry extends HungerLevel("Hungry", 40.0)
    case VeryHungry extends HungerLevel("Very Hungry", 20.0)
    case Starving extends HungerLevel("Starving", 0.0)

  // Food values using enum and extension methods
  enum CropType(
      val displayName: String,
      val hungerValue: Double,
      val healthValue: Double
  ):
    case Wheat extends CropType("Wheat", 15.0, 2.0)
    case Corn extends CropType("Corn", 25.0, 3.0)
    case Carrot extends CropType("Carrot", 12.0, 5.0)
    case Tomato extends CropType("Tomato", 10.0, 4.0)
    case Spinach extends CropType("Spinach", 18.0, 4.0)
    case Lettuce extends CropType("Lettuce", 8.0, 2.0)
    case Onion extends CropType("Onion", 14.0, 3.0)
    case Pumpkin extends CropType("Pumpkin", 30.0, 1.0)

  // System state
  private var currentHunger: Double = 100.0
  private var lastUpdateTime: Long = System.currentTimeMillis()
  private var lastStarvationDamage: Long = System.currentTimeMillis()

  // Configuration constants
  private val MaxHunger = 100.0
  private val HungerDecayPerSecond = 1.5
  private val StarvationThreshold = 20.0
  private val StarvationDamageInterval = 100000L // 100 seconds

  /** Initialize the hunger system */
  def initialize(): Unit =
    println("[HungerSystem] Initialized - Modern Scala 3 implementation")
    lastUpdateTime = System.currentTimeMillis()

  /** Update hunger system each frame */
  def update(): Unit =
    val currentTime = System.currentTimeMillis()
    val deltaTime = (currentTime - lastUpdateTime) / 1000.0
    lastUpdateTime = currentTime

    // Decrease hunger over time
    currentHunger =
      math.max(0.0, currentHunger - (HungerDecayPerSecond * deltaTime))

    // Check for starvation damage
    if isStarving && (currentTime - lastStarvationDamage) >= StarvationDamageInterval
    then
      playerLifeSystem.takeDamage(1)
      lastStarvationDamage = currentTime
      println(
        s"[HungerSystem] Starvation damage! Health: ${playerLifeSystem.getCurrentHearts}/${playerLifeSystem.getMaxHearts}"
      )

  /** Eat a crop to restore hunger and health */
  def eatCrop(cropName: String): Boolean =
    CropType.values.find(_.toString.toLowerCase == cropName.toLowerCase) match
      case Some(cropType) =>
        // Check if eating would provide any benefit
        val wouldRestoreHunger = currentHunger < MaxHunger
        val wouldRestoreHealth =
          cropType.healthValue > 0 && !playerLifeSystem.isFullHealth

        if !wouldRestoreHunger && !wouldRestoreHealth then
          println(
            s"[HungerSystem] No need to eat ${cropType.displayName} - already at full hunger and health!"
          )
          return false

        // Determine what benefits the crop would provide
        val hungerBenefit =
          if wouldRestoreHunger then cropType.hungerValue else 0.0
        val healthBenefit =
          if wouldRestoreHealth then cropType.healthValue else 0.0

        // Apply the benefits
        if wouldRestoreHunger then
          val oldHunger = currentHunger
          currentHunger =
            math.min(MaxHunger, currentHunger + cropType.hungerValue)
          val actualHungerRestored = currentHunger - oldHunger

        if wouldRestoreHealth then
          playerLifeSystem.heal(cropType.healthValue.toInt)

        // Create informative message
        val benefits = List(
          if hungerBenefit > 0 then Some(f"+${hungerBenefit}%.1f hunger")
          else None,
          if healthBenefit > 0 then Some(f"+${healthBenefit}%.0f health")
          else None
        ).flatten

        val benefitText =
          if benefits.nonEmpty then benefits.mkString(", ") else "no benefit"
        println(s"[HungerSystem] Ate ${cropType.displayName}: $benefitText")
        true

      case None =>
        println(s"[HungerSystem] Unknown crop: $cropName")
        false

  // Convenient getters using Scala 3 features
  def getHungerPercentage: Double = (currentHunger / MaxHunger) * 100.0
  def getCurrentHunger: Double = currentHunger
  def getMaxHunger: Double = MaxHunger
  def isStarving: Boolean = currentHunger <= StarvationThreshold

  def getHungerLevel: HungerLevel =
    val percentage = getHungerPercentage
    HungerLevel.values
      .find(level => percentage >= level.threshold)
      .getOrElse(HungerLevel.Starving)

  def getStatusText: String = getHungerLevel.displayName

  /** Check if eating a specific crop would provide any benefit */
  def canBenefitFromEating(cropName: String): Boolean =
    CropType.values.find(_.toString.toLowerCase == cropName.toLowerCase) match
      case Some(cropType) =>
        val wouldRestoreHunger = currentHunger < MaxHunger
        val wouldRestoreHealth =
          cropType.healthValue > 0 && !playerLifeSystem.isFullHealth
        wouldRestoreHunger || wouldRestoreHealth
      case None => false

  /** Get a detailed status for UI display */
  def getDetailedStatus: String =
    val hungerPercent = getHungerPercentage
    val healthStatus =
      if playerLifeSystem.isFullHealth then "Full"
      else s"${playerLifeSystem.getCurrentLife}/${playerLifeSystem.getMaxLife}"
    f"Hunger: ${hungerPercent}%.0f%% (${getStatusText}) | Health: $healthStatus"

  /** Debug method to reduce hunger for testing */
  def debugReduceHunger(amount: Double): Unit =
    currentHunger = math.max(0.0, currentHunger - amount)

end HungerSystem
