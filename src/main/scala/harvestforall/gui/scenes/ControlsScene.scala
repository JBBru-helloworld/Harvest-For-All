package harvestforall.gui.scenes

import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{
  VBox,
  HBox,
  StackPane,
  Background,
  BackgroundFill,
  CornerRadii,
  Border,
  BorderStroke,
  BorderWidths,
  BorderStrokeStyle
}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.{Font, FontWeight, TextAlignment}
import scalafx.scene.paint.{Color, LinearGradient, CycleMethod, Stop}
import scalafx.scene.effect.{DropShadow, BlurType}
import scalafx.scene.input.{KeyEvent, KeyCode}
import scalafx.Includes._
import harvestforall.gui.managers.SceneManager
import harvestforall.core.GameState
import harvestforall.gui.utils.FontManager

/** Controls scene for displaying game controls and instructions
  *
  * Shows comprehensive control scheme and gameplay instructions
  */
class ControlsScene(sceneManager: SceneManager, gameState: GameState):

  private val SCENE_WIDTH = 1200
  private val SCENE_HEIGHT = 800

  /** Create the controls scene
    */
  def getScene: Scene =
    val scene = new Scene(SCENE_WIDTH, SCENE_HEIGHT):
      fill = Color.web("#1B4F20") // Match main menu style
      root = createControlsLayout()

      // Handle ESC key to go back
      onKeyPressed = (event: KeyEvent) =>
        if event.code == KeyCode.Escape then sceneManager.switchToMainMenu()

    scene

  /** Create the main controls layout
    */
  private def createControlsLayout(): StackPane =
    val mainPane = new StackPane()

    // Gradient background
    val backgroundBox = new VBox():
      background = Background(
        Array(
          BackgroundFill(
            LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = CycleMethod.NoCycle,
              stops = List(
                Stop(0, Color.web("#2E7D32")), // Dark green top
                Stop(0.5, Color.web("#1B5E20")), // Darker green middle
                Stop(1, Color.web("#0D4F14")) // Very dark green bottom
              )
            ),
            CornerRadii.Empty,
            Insets.Empty
          )
        )
      )
      prefWidth = SCENE_WIDTH
      prefHeight = SCENE_HEIGHT

    // Content container - matching AboutScene style
    val contentBox = new VBox():
      spacing = 25
      alignment = Pos.TopCenter
      padding = Insets(40, 60, 40, 60)
      maxWidth = 900
      background = Background(
        Array(
          BackgroundFill(
            Color.web("#000000", 0.25),
            CornerRadii(15),
            Insets.Empty
          )
        )
      )
      border = Border(
        BorderStroke(
          Color.web("#4CAF50"),
          BorderStrokeStyle.Solid,
          CornerRadii(15),
          BorderWidths(3)
        )
      )

    // Title - matching AboutScene style
    val titleLabel = new Label("Game Controls"):
      font = FontManager.titleFont
      textFill = Color.web("#E8F5E8")
      effect = new DropShadow:
        blurType = BlurType.Gaussian
        color = Color.Black
        offsetX = 3
        offsetY = 3
        radius = 4

    // Subtitle - matching AboutScene style
    val subtitleLabel = new Label(
      "Complete Control Reference for Harvest for All"
    ):
      font = FontManager.subHeaderFont
      textFill = Color.web("#81C784")
      wrapText = true
      maxWidth = 800
      textAlignment = TextAlignment.Center

    // Controls content
    val controlsText = new Label:
      text = s"""
        |${"-" * 60}
        |
        |MOVEMENT CONTROLS:
        |• WASD Keys - Move character up/down/left/right
        |• Arrow Keys - Alternative movement controls
        |
        |FARMING ACTIONS:
        |• E Key - Plant seeds or Harvest crops (context-sensitive)
        |• R Key - Water crops with watering can
        |
        |INVENTORY & INTERFACE:
        |• I Key - Open/Close inventory system
        |• ENTER Key - Use selected item in inventory
        |• WASD Keys/Arrow Keys - Navigate inventory items
        |
        |GAME INFORMATION:
        |• T Key - View tutorial messages
        |• C Key - Show control help
        |• H Key - Get farming tips and advice
        |
        |GAME CONTROL:
        |• P Key or SPACE - Pause game
        |• ESC Key - Exit fullscreen or go back
        |
        |VILLAGE & INTERACTION:
        |• ENTER Key - Enter village market (when near road)
        |• ENTER/SPACE Keys - Continue dialogue text
        |
        |TREASURE CHEST INTERACTION:
        |• ENTER Key - Open treasure chest (when near)
        |
        |${"-" * 60}
        |
        |GAMEPLAY TIPS:
        |• Plant seeds on brown soil tiles
        |• Water crops regularly for faster growth
        |• Harvest when crops are fully grown
        |• Visit village to buy/sell items
        |• Check your inventory to see available items
        |• Different crops have different growth rates
        |
        |""".stripMargin
      font = FontManager.labelFont
      textFill = Color.web("#E0F7E0")
      wrapText = true
      maxWidth = 800
      lineSpacing = 1.5
      textAlignment = TextAlignment.Left

    // Scroll pane for controls content
    val scrollPane = new ScrollPane:
      content = controlsText
      fitToWidth = true
      fitToHeight = false
      prefHeight = 450 // Slightly reduced to account for subtitle
      style = "-fx-background: transparent; -fx-background-color: transparent;"
      pannable = true

    // Back button
    val backButton = new Button("Back to Main Menu"):
      prefWidth = 250
      prefHeight = 50
      font = FontManager.buttonFont
      textFill = Color.White
      background = Background(
        Array(
          BackgroundFill(
            LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = CycleMethod.NoCycle,
              stops = List(
                Stop(0, Color.web("#4CAF50")),
                Stop(1, Color.web("#388E3C"))
              )
            ),
            CornerRadii(5),
            Insets.Empty
          )
        )
      )
      onAction = _ => sceneManager.switchToMainMenu()

      // Button hover effects
      onMouseEntered = _ =>
        background = Background(
          Array(
            BackgroundFill(
              LinearGradient(
                startX = 0,
                startY = 0,
                endX = 0,
                endY = 1,
                proportional = true,
                cycleMethod = CycleMethod.NoCycle,
                stops = List(
                  Stop(0, Color.web("#66BB6A")),
                  Stop(1, Color.web("#4CAF50"))
                )
              ),
              CornerRadii(5),
              Insets.Empty
            )
          )
        )

      onMouseExited = _ =>
        background = Background(
          Array(
            BackgroundFill(
              LinearGradient(
                startX = 0,
                startY = 0,
                endX = 0,
                endY = 1,
                proportional = true,
                cycleMethod = CycleMethod.NoCycle,
                stops = List(
                  Stop(0, Color.web("#4CAF50")),
                  Stop(1, Color.web("#388E3C"))
                )
              ),
              CornerRadii(5),
              Insets.Empty
            )
          )
        )

    // Assemble content
    contentBox.children = List(
      titleLabel,
      subtitleLabel,
      scrollPane,
      backButton
    )

    // Position content in center
    mainPane.children = List(backgroundBox, contentBox)
    StackPane.setAlignment(contentBox, Pos.Center)

    mainPane

end ControlsScene
