package harvestforall.game.ui

import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.scene.image.Image
import harvestforall.graphics.CropSpriteManager
import harvestforall.gui.utils.FontManager
import harvestforall.core.GameState
import scala.collection.mutable

/** Enhanced inventory system using crop sprites from the crop manager Displays
  * farming-specific items with interactive UI
  */
class FarmingInventorySystem:

  // Inventory UI configuration
  private val FRAME_WIDTH = 600
  private val FRAME_HEIGHT = 400
  private val SLOT_SIZE = 48
  private val SLOT_SPACING = 8
  private val SLOTS_PER_ROW = 10
  private val MAX_INVENTORY_SIZE = 40

  // Selection cursor
  private var selectedSlotCol = 0
  private var selectedSlotRow = 0
  private var isInventoryOpen = false

  // GameState integration for synchronized inventory
  private var gameState: Option[GameState] = None

  // Use LinkedHashMap to maintain insertion order for consistent item ordering
  private val inventory: mutable.LinkedHashMap[String, Int] =
    mutable.LinkedHashMap.empty

  // Reference to crop sprite manager
  private var cropSpriteManager: Option[CropSpriteManager] = None

  // Font for inventory text
  private var inventoryFont: Font = _

  def initialize(spriteManager: CropSpriteManager): Unit =
    try
      cropSpriteManager = Some(spriteManager)
      inventoryFont = FontManager.buttonFont // 16pt upheaval font

      // Add initial seeds for farming (grouped together)
      addItem("wheat_seeds", 10)
      addItem("corn_seeds", 8)
      addItem("carrot_seeds", 12)
      addItem("tomato_seeds", 6)
      addItem("spinach_seeds", 8)

      // Add some initial harvested crops for eating (grouped together)
      addItem("wheat", 3)
      addItem("corn", 2)
      addItem("carrot", 4)
      addItem("tomato", 1)
      addItem("spinach", 2)

      println(
        "[FarmingInventorySystem] Initialized with seeds and crops - Ready for farming!"
      )
    catch
      case ex: Exception =>
        println(
          s"[FarmingInventorySystem] Error initializing: ${ex.getMessage}"
        )
        inventoryFont = FontManager.getCustomFont(16) // Fallback font

  def toggleInventory(): Unit =
    isInventoryOpen = !isInventoryOpen
    if isInventoryOpen then println("[FarmingInventorySystem] Inventory opened")
    else println("[FarmingInventorySystem] Inventory closed")

  def handleInput(key: String): Unit =
    if isInventoryOpen then
      key match
        case "LEFT" | "A" | "Left" =>
          selectedSlotCol = math.max(0, selectedSlotCol - 1)
        case "RIGHT" | "D" | "Right" =>
          selectedSlotCol = math.min(SLOTS_PER_ROW - 1, selectedSlotCol + 1)
        case "UP" | "W" | "Up" =>
          selectedSlotRow = math.max(0, selectedSlotRow - 1)
        case "DOWN" | "S" | "Down" =>
          selectedSlotRow = math.min(3, selectedSlotRow + 1) // 4 rows max
        case "ENTER" | "Enter" | "Return" =>
          useSelectedItem()
        case "I" =>
          toggleInventory()
        case _ => // Ignore other keys
  def addItem(cropType: String, quantity: Int): Boolean =
    // Check if we have space for new item types (max 40 different items, not total quantity)
    if inventory.contains(cropType) || inventory.size < MAX_INVENTORY_SIZE then
      val current = inventory.getOrElse(cropType, 0)
      inventory(cropType) = current + quantity

      // Sync with GameState inventory if available
      gameState.foreach(_.addToInventory(cropType, quantity))

      println(
        s"[FarmingInventorySystem] Added $quantity $cropType (total: ${inventory(cropType)})"
      )
      true
    else
      println("[FarmingInventorySystem] Inventory full! (40 item types max)")
      false

  def removeItem(cropType: String, quantity: Int): Boolean =
    val current = inventory.getOrElse(cropType, 0)
    if current >= quantity then
      val newQuantity = current - quantity
      if newQuantity <= 0 then
        inventory.remove(cropType)
        println(
          s"[FarmingInventorySystem] Removed $quantity $cropType (item removed from inventory)"
        )
      else
        inventory(cropType) = newQuantity
        println(
          s"[FarmingInventorySystem] Removed $quantity $cropType (remaining: $newQuantity)"
        )

      // Sync with GameState inventory if available
      gameState.foreach(_.removeFromInventory(cropType, quantity))

      true
    else
      println(
        s"[FarmingInventorySystem] Cannot remove $quantity $cropType - only have $current"
      )
      false

  /** Set the GameState for inventory synchronization */
  def setGameState(gs: GameState): Unit =
    gameState = Some(gs)
    // Sync existing inventory to GameState
    for (cropType, quantity) <- inventory do
      gs.addToInventory(cropType, quantity)

  def hasItem(cropType: String, quantity: Int = 1): Boolean =
    inventory.getOrElse(cropType, 0) >= quantity

  def getItemCount(cropType: String): Int =
    inventory.getOrElse(cropType, 0)

  private def getTotalItems: Int =
    inventory.values.sum

  def getSelectedItemName: Option[String] = {
    val items = inventory.keys.toVector
    val index = selectedSlotRow * SLOTS_PER_ROW + selectedSlotCol
    if (index < items.size) Some(items(index)) else None
  }

  // Add method to get ordered inventory for consistent access
  def getInventory: mutable.LinkedHashMap[String, Int] = inventory

  // Optional reference to hunger system for eating crops
  private var hungerSystem: Option[Any] = None // Will be set by game scene

  def setHungerSystem(system: Any): Unit =
    hungerSystem = Some(system)

  /** Synchronize the local inventory with GameState inventory */
  def syncWithGameState(): Unit =
    gameState.foreach { gs =>
      // Clear local inventory and sync with GameState
      inventory.clear()
      for (item, quantity) <- gs.inventory do
        if quantity > 0 then inventory(item) = quantity

      // Clean up any old potato references and reorganize
      cleanupAndReorganizeInventory()

      println(
        s"[FarmingInventorySystem] Synced inventory with GameState: ${inventory.toMap}"
      )
    }

  /** Clean up old potato references and reorganize inventory for better UX */
  private def cleanupAndReorganizeInventory(): Unit =
    // Remove any potato items from old saves
    inventory.remove("potato")
    inventory.remove("potato_seeds")

    // Create a new organized inventory with seeds first, then crops
    val originalInventory = inventory.toMap
    inventory.clear()

    // Add seeds first (better organization)
    val seedItems =
      originalInventory.filter(_._1.endsWith("_seeds")).toSeq.sortBy(_._1)
    val cropItems =
      originalInventory.filterNot(_._1.endsWith("_seeds")).toSeq.sortBy(_._1)

    // Re-add in organized order: seeds first, then crops
    (seedItems ++ cropItems).foreach { case (item, quantity) =>
      inventory(item) = quantity
    }

    println(
      "[FarmingInventorySystem] Cleaned up and reorganized inventory - seeds first, then crops"
    )

  private def useSelectedItem(): Unit =
    val items = inventory.keys.toArray
    val selectedIndex = selectedSlotRow * SLOTS_PER_ROW + selectedSlotCol

    if selectedIndex < items.length then
      val selectedItem = items(selectedIndex)
      val quantity = inventory(selectedItem)

      if quantity > 0 then
        // Check if it's a crop (not seeds) that can be eaten
        if !selectedItem.endsWith("_seeds") then
          println(
            s"[FarmingInventorySystem] Attempting to eat $selectedItem..."
          )

          // Try to eat the crop through hunger system
          hungerSystem match
            case Some(hs: harvestforall.game.systems.HungerSystem) =>
              val wasEaten = hs.eatCrop(selectedItem)
              if wasEaten then
                removeItem(selectedItem, 1)
                println(
                  s"[FarmingInventorySystem] Successfully consumed $selectedItem!"
                )
              else
                println(
                  s"[FarmingInventorySystem] Could not eat $selectedItem - check hunger/health status"
                )
            case _ =>
              // Fallback if hunger system not available - allow eating but with warning
              println(
                s"[FarmingInventorySystem] Consumed $selectedItem (hunger system not available)"
              )
              removeItem(selectedItem, 1)
        else
          println(
            s"[FarmingInventorySystem] Cannot eat seeds! Use them for planting."
          )
      else println("[FarmingInventorySystem] No items to use")

  def render(
      gc: GraphicsContext,
      screenWidth: Double,
      screenHeight: Double
  ): Unit =
    if isInventoryOpen then drawInventoryFrame(gc, screenWidth, screenHeight)

  private def drawInventoryFrame(
      gc: GraphicsContext,
      screenWidth: Double,
      screenHeight: Double
  ): Unit =
    // Calculate frame position (center of screen)
    val frameX = (screenWidth - FRAME_WIDTH) / 2
    val frameY = (screenHeight - FRAME_HEIGHT) / 2

    // Draw main inventory background
    gc.fill =
      Color.color(0.2, 0.2, 0.3, 0.9) // Dark semi-transparent background
    gc.fillRect(frameX, frameY, FRAME_WIDTH, FRAME_HEIGHT)

    // Draw border
    gc.stroke = Color.color(0.8, 0.6, 0.4) // Golden border
    gc.lineWidth = 3
    gc.strokeRect(frameX, frameY, FRAME_WIDTH, FRAME_HEIGHT)

    // Draw title
    gc.fill = Color.White
    gc.font = FontManager.headerFont // 28pt upheaval font
    gc.fillText("Farm Inventory", frameX + 20, frameY + 30)

    // Draw inventory slots
    drawInventorySlots(gc, frameX, frameY)

    // Draw cursor
    drawCursor(gc, frameX, frameY)

    // Draw item details
    drawItemDetails(gc, frameX, frameY)

  private def drawInventorySlots(
      gc: GraphicsContext,
      frameX: Double,
      frameY: Double
  ): Unit =
    val slotStartX = frameX + 20
    val slotStartY = frameY + 50

    var slotX = slotStartX
    var slotY = slotStartY

    val items = inventory.keys.toArray

    for (row <- 0 until 4) do
      for (col <- 0 until SLOTS_PER_ROW) do
        val slotIndex = row * SLOTS_PER_ROW + col

        // Draw slot background
        gc.fill = Color.color(0.3, 0.3, 0.4)
        gc.fillRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE)

        // Draw slot border
        gc.stroke = Color.color(0.6, 0.6, 0.7)
        gc.lineWidth = 1
        gc.strokeRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE)

        // Draw item if present
        if slotIndex < items.length then
          val itemName = items(slotIndex)
          val quantity = inventory(itemName)
          val isSeeds = itemName.endsWith("_seeds")

          // Get the base crop name (remove "_seeds" suffix if present)
          val baseCropName = if isSeeds then itemName.dropRight(6) else itemName

          // Draw slot background with different colors for seeds vs crops
          if isSeeds then
            gc.fill = Color.color(0.4, 0.3, 0.2) // Brown background for seeds
          else
            gc.fill = Color.color(0.2, 0.4, 0.3) // Green background for crops
          gc.fillRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE)

          // Draw crop sprite with appropriate stages
          cropSpriteManager.foreach { manager =>
            // For seeds: use the dedicated seeds sprite (SEEDS_COL)
            // For harvested crops: use stage 4 (final harvest stage, array index 4 = column 7)
            if isSeeds then
              manager.getSeedsSprite(baseCropName).foreach { sprite =>
                gc.drawImage(
                  sprite,
                  slotX + 8,
                  slotY + 8,
                  SLOT_SIZE - 16,
                  SLOT_SIZE - 16
                )
              }
            else
              manager.getCropSprite(baseCropName, 4).foreach {
                sprite => // Stage 4 = final harvest stage
                  gc.drawImage(
                    sprite,
                    slotX + 8,
                    slotY + 8,
                    SLOT_SIZE - 16,
                    SLOT_SIZE - 16
                  )
              }
          }

          // Draw seed indicator
          if isSeeds then
            gc.fill = Color.color(0.8, 0.6, 0.4)
            gc.font = FontManager.smallFont
            gc.fillText("S", slotX + 2, slotY + 12) // "S" for seeds

          // Draw quantity if > 1
          if quantity > 1 then
            gc.fill = Color.White
            gc.font = FontManager.smallFont // 12pt upheaval font
            val quantityText = quantity.toString
            gc.fillText(
              quantityText,
              slotX + SLOT_SIZE - 15,
              slotY + SLOT_SIZE - 5
            )

        slotX += SLOT_SIZE + SLOT_SPACING

      // Next row
      slotX = slotStartX
      slotY += SLOT_SIZE + SLOT_SPACING

  private def drawCursor(
      gc: GraphicsContext,
      frameX: Double,
      frameY: Double
  ): Unit =
    val slotStartX = frameX + 20
    val slotStartY = frameY + 50

    val cursorX = slotStartX + selectedSlotCol * (SLOT_SIZE + SLOT_SPACING)
    val cursorY = slotStartY + selectedSlotRow * (SLOT_SIZE + SLOT_SPACING)

    // Draw selection cursor
    gc.stroke = Color.Yellow
    gc.lineWidth = 3
    gc.strokeRect(cursorX - 2, cursorY - 2, SLOT_SIZE + 4, SLOT_SIZE + 4)

  private def drawItemDetails(
      gc: GraphicsContext,
      frameX: Double,
      frameY: Double
  ): Unit =
    val items = inventory.keys.toArray
    val selectedIndex = selectedSlotRow * SLOTS_PER_ROW + selectedSlotCol

    if selectedIndex < items.length then
      val selectedCrop = items(selectedIndex)
      val quantity = inventory(selectedCrop)

      // Draw item details panel
      val detailsX = frameX + 20
      val detailsY =
        frameY + FRAME_HEIGHT - 120 // Increased height for extra info
      val detailsWidth = FRAME_WIDTH - 40
      val detailsHeight = 100 // Increased from 80 to 100

      gc.fill = Color.color(0.1, 0.1, 0.2, 0.8)
      gc.fillRect(detailsX, detailsY, detailsWidth, detailsHeight)

      gc.stroke = Color.color(0.6, 0.6, 0.7)
      gc.lineWidth = 1
      gc.strokeRect(detailsX, detailsY, detailsWidth, detailsHeight)

      // Determine if selected item is seeds
      val isSeeds = selectedCrop.endsWith("_seeds")
      val baseCropName =
        if isSeeds then selectedCrop.dropRight(6) else selectedCrop
      val itemType = if isSeeds then "Seeds" else "Harvested Crop"

      // Draw item info
      gc.fill = Color.White
      gc.font = FontManager.subtitleFont // 18pt upheaval font

      // Create consistent display name for better alignment
      val displayName =
        if isSeeds then s"${baseCropName.capitalize} Seeds"
        else baseCropName.capitalize

      gc.fillText(displayName, detailsX + 10, detailsY + 25)

      gc.font = FontManager.labelFont // 14pt upheaval font
      gc.fillText(s"Quantity: $quantity", detailsX + 10, detailsY + 45)
      gc.fillText(s"Type: $itemType", detailsX + 10, detailsY + 65)

      // Add eating status for crops
      if !isSeeds then
        hungerSystem match
          case Some(hs: harvestforall.game.systems.HungerSystem) =>
            val canEat = hs.canBenefitFromEating(baseCropName)
            val eatStatus =
              if canEat then "Press ENTER to eat" else "No benefit from eating"
            gc.fill = if canEat then Color.LightGreen else Color.LightGray
            gc.fillText(eatStatus, detailsX + 10, detailsY + 80)
          case _ =>
            gc.fill = Color.Yellow
            gc.fillText("Press ENTER to eat", detailsX + 10, detailsY + 80)

      // Draw large sprite in details with appropriate type
      cropSpriteManager.foreach { manager =>
        if isSeeds then
          manager.getSeedsSprite(baseCropName).foreach { sprite =>
            gc.drawImage(
              sprite,
              detailsX + detailsWidth - 80,
              detailsY + 10,
              60,
              60
            )
          }
        else
          manager.getCropSprite(baseCropName, 4).foreach {
            sprite => // Final harvest stage
              gc.drawImage(
                sprite,
                detailsX + detailsWidth - 80,
                detailsY + 10,
                60,
                60
              )
          }
      }

  def isOpen: Boolean = isInventoryOpen

  def getInventoryContents: Map[String, Int] = inventory.toMap

  // Add getter methods for selected slot positions
  def getSelectedSlotRow: Int = selectedSlotRow
  def getSelectedSlotCol: Int = selectedSlotCol

  // Debug method to see inventory state
  def printInventory(): Unit =
    println("[FarmingInventorySystem] Current inventory:")
    for (cropType, quantity) <- inventory do println(s"  $cropType: $quantity")

end FarmingInventorySystem
