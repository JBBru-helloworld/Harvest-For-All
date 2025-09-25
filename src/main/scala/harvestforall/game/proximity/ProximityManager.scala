package harvestforall.game.proximity

import harvestforall.graphics.{FarmTileManager, FarmPlayer}

// What kinds of proximity events can happen
enum ProximityEvent:
  case VillageEntered, VillageExited, TreasureChestFound, TreasureChestLost

// Interface for things that want to know about proximity events
trait ProximityEventListener:
  def onProximityEvent(event: ProximityEvent): Unit

/** Manages proximity detection for interactive elements in the game world
  *
  * I made this to detect when the player gets close to important stuff like
  * villages and treasure chests. Pretty cool how it fires events!
  */
class ProximityManager(tileManager: FarmTileManager, player: FarmPlayer):

  // What we know about player's current proximity to stuff
  case class ProximityState(
      isNearVillage: Boolean = false,
      isNearTreasureChest: Boolean = false,
      lastUpdateTime: Long = System.currentTimeMillis()
  )

  private var currentState = ProximityState()
  private var eventListeners: List[ProximityEventListener] = List.empty

  /** Add a proximity event listener */
  def addListener(listener: ProximityEventListener): Unit =
    eventListeners = listener :: eventListeners

  /** Remove a proximity event listener */
  def removeListener(listener: ProximityEventListener): Unit =
    eventListeners = eventListeners.filterNot(_ == listener)

  /** Update proximity checks for all interactive elements */
  def updateProximity(): Unit =
    val newState = ProximityState(
      isNearVillage = checkVillageProximity(),
      isNearTreasureChest = checkTreasureChestProximity(),
      lastUpdateTime = System.currentTimeMillis()
    )

    // Fire events for state changes (if anything changed)
    handleStateChange(currentState, newState)
    currentState = newState

  /** Check if player is near a village entrance */
  private def checkVillageProximity(): Boolean =
    tileManager.isNearVillage(
      player.worldX,
      player.worldY,
      proximityRange = 1
    )

  /** Check if player is near a treasure chest */
  private def checkTreasureChestProximity(): Boolean =
    isNearTreasureChest(
      player.worldX,
      player.worldY,
      proximityRange = 1
    )

  /** Check if player is near a treasure chest tile (tile 35) using functional
    * approach
    */
  private def isNearTreasureChest(
      playerWorldX: Double,
      playerWorldY: Double,
      proximityRange: Int = 1
  ): Boolean =
    val playerCol = (playerWorldX / tileManager.tileSize).toInt
    val playerRow = (playerWorldY / tileManager.tileSize).toInt

    // Generate nearby tile coordinates
    val nearbyTiles = for
      col <- (playerCol - proximityRange) to (playerCol + proximityRange)
      row <- (playerRow - proximityRange) to (playerRow + proximityRange)
      if isValidTilePosition(row, col)
    yield (col, row)

    // Check if any nearby tile is a treasure chest (tile 35) - functional magic actaully super cool AHAHAH!
    nearbyTiles.exists { (col, row) =>
      tileManager.mapTileNum(tileManager.currentMap)(col)(row) == 35
    }

  /** Validate if tile position is within world bounds */
  private def isValidTilePosition(row: Int, col: Int): Boolean =
    row >= 0 && row < tileManager.maxWorldRow &&
      col >= 0 && col < tileManager.maxWorldCol

  /** Handle state changes and fire appropriate events */
  private def handleStateChange(
      oldState: ProximityState,
      newState: ProximityState
  ): Unit =
    // Village proximity events
    if !oldState.isNearVillage && newState.isNearVillage then
      fireEvent(ProximityEvent.VillageEntered) // Just entered village area!
    else if oldState.isNearVillage && !newState.isNearVillage then
      fireEvent(ProximityEvent.VillageExited) // Left village area

    // Treasure chest proximity events
    if !oldState.isNearTreasureChest && newState.isNearTreasureChest then
      fireEvent(ProximityEvent.TreasureChestFound) // Found treasure!
    else if oldState.isNearTreasureChest && !newState.isNearTreasureChest then
      fireEvent(ProximityEvent.TreasureChestLost) // Walked away from treasure

  /** Fire proximity event to all listeners */
  private def fireEvent(event: ProximityEvent): Unit =
    eventListeners.foreach(_.onProximityEvent(event))

  // Handy getters so other classes can check what's happening
  def isNearVillage: Boolean = currentState.isNearVillage
  def isNearTreasureChest: Boolean = currentState.isNearTreasureChest
  def getCurrentState: ProximityState = currentState

// Companion object with factory methods and utilities
object ProximityManager:
  /** Create a proximity manager with console logging */
  def withConsoleLogging(
      tileManager: FarmTileManager,
      player: FarmPlayer
  ): ProximityManager =
    val manager = new ProximityManager(tileManager, player)
    manager.addListener(ConsoleProximityListener())
    manager

  /** Default proximity event listener that logs to console */
  case class ConsoleProximityListener() extends ProximityEventListener:
    def onProximityEvent(event: ProximityEvent): Unit =
      event match
        case ProximityEvent.VillageEntered =>
          println("Near village! Press ENTER to visit the village market.")
        case ProximityEvent.VillageExited =>
          println("Left village area.")
        case ProximityEvent.TreasureChestFound =>
          println("Mysterious treasure chest found! Press ENTER to examine it.")
        case ProximityEvent.TreasureChestLost =>
          println("Left treasure chest area.")
