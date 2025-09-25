package harvestforall.gui.components

import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, Priority}
import scalafx.scene.paint.Color
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.Font
import scalafx.Includes._
import harvestforall.core.GameState
import harvestforall.gui.utils.FontManager

enum MenuAction:
  case Pause, Save, MainMenu, LearnMore

/** The menu bar at the top of the farm scene I wanted to make this really nice
  * looking with gradients and hover effects Keeps track of money and
  * sustainability in real-time
  */
class FarmMenuBar(using gameState: GameState):

  // Menu bar labels (keeping references so I can update them easily)
  private val moneyLabel = new Label(f"Money: $$${gameState.currency}%.2f"):
    font = FontManager.buttonFont
    textFill = Color.White
    style = "-fx-padding: 0 20 0 20;"

  private val sustainabilityLabel = new Label(
    f"Sustainability: ${gameState.globalSustainabilityRating}%.1f%%"
  ):
    font = FontManager.buttonFont
    textFill = Color.White
    style = "-fx-padding: 0 20 0 20;"

  def createMenuBar(actionHandler: MenuAction => Unit): HBox =
    // Making buttons that look fancy - I love gradient effects!
    def menuButton(text: String, action: () => Unit): Button =
      val button = new Button(text):
        prefWidth = 140
        prefHeight = 70
        font = FontManager.buttonFont
        textFill = Color.White
        background = scalafx.scene.layout.Background(
          Array(
            scalafx.scene.layout.BackgroundFill(
              scalafx.scene.paint.LinearGradient(
                startX = 0,
                startY = 0,
                endX = 0,
                endY = 1,
                proportional = true,
                cycleMethod = scalafx.scene.paint.CycleMethod.NoCycle,
                stops = List(
                  scalafx.scene.paint.Stop(0, Color.web("#4CAF50")),
                  scalafx.scene.paint.Stop(1, Color.web("#388E3C"))
                )
              ),
              scalafx.scene.layout.CornerRadii(8),
              Insets.Empty
            )
          )
        )
        border = scalafx.scene.layout.Border(
          scalafx.scene.layout.BorderStroke(
            Color.web("#2E7D32"),
            scalafx.scene.layout.BorderStrokeStyle.Solid,
            scalafx.scene.layout.CornerRadii(8),
            scalafx.scene.layout.BorderWidths(2)
          )
        )
        effect = new scalafx.scene.effect.DropShadow:
          blurType = scalafx.scene.effect.BlurType.Gaussian
          color = Color.Black
          offsetX = 2
          offsetY = 2
          radius = 4
          spread = 0.7
        onAction = _ => action()

      // Hover effects - makes buttons feel alive when you hover over them!
      button.onMouseEntered = _ =>
        button.background = scalafx.scene.layout.Background(
          Array(
            scalafx.scene.layout.BackgroundFill(
              scalafx.scene.paint.LinearGradient(
                startX = 0,
                startY = 0,
                endX = 0,
                endY = 1,
                proportional = true,
                cycleMethod = scalafx.scene.paint.CycleMethod.NoCycle,
                stops = List(
                  scalafx.scene.paint.Stop(0, Color.web("#66BB6A")),
                  scalafx.scene.paint.Stop(1, Color.web("#4CAF50"))
                )
              ),
              scalafx.scene.layout.CornerRadii(8),
              Insets.Empty
            )
          )
        )

      button.onMouseExited = _ =>
        button.background = scalafx.scene.layout.Background(
          Array(
            scalafx.scene.layout.BackgroundFill(
              scalafx.scene.paint.LinearGradient(
                startX = 0,
                startY = 0,
                endX = 0,
                endY = 1,
                proportional = true,
                cycleMethod = scalafx.scene.paint.CycleMethod.NoCycle,
                stops = List(
                  scalafx.scene.paint.Stop(0, Color.web("#4CAF50")),
                  scalafx.scene.paint.Stop(1, Color.web("#388E3C"))
                )
              ),
              scalafx.scene.layout.CornerRadii(8),
              Insets.Empty
            )
          )
        )

      button

    val pauseButton = menuButton("Pause", () => actionHandler(MenuAction.Pause))
    val saveButton =
      menuButton("Save Game", () => actionHandler(MenuAction.Save))
    val backButton =
      menuButton("Main Menu", () => actionHandler(MenuAction.MainMenu))
    val learnMoreButton =
      menuButton("Learn More", () => actionHandler(MenuAction.LearnMore))

    updateSustainabilityColor()

    new HBox:
      spacing = 20
      padding = Insets(20, 30, 20, 30)
      alignment = Pos.Center
      fillHeight = true
      maxWidth = Double.MaxValue
      background = scalafx.scene.layout.Background(
        Array(
          scalafx.scene.layout.BackgroundFill(
            scalafx.scene.paint.LinearGradient(
              startX = 0,
              startY = 0,
              endX = 0,
              endY = 1,
              proportional = true,
              cycleMethod = scalafx.scene.paint.CycleMethod.NoCycle,
              stops = List(
                scalafx.scene.paint.Stop(0, Color.web("#2E7D32")),
                scalafx.scene.paint.Stop(0.5, Color.web("#1B5E20")),
                scalafx.scene.paint.Stop(1, Color.web("#0D4F14"))
              )
            ),
            scalafx.scene.layout.CornerRadii(12),
            Insets.Empty
          ),
          scalafx.scene.layout.BackgroundFill(
            Color.web("#000000", 0.22),
            scalafx.scene.layout.CornerRadii(12),
            Insets.Empty
          )
        )
      )
      border = scalafx.scene.layout.Border(
        scalafx.scene.layout.BorderStroke(
          Color.web("#4CAF50"),
          scalafx.scene.layout.BorderStrokeStyle.Solid,
          scalafx.scene.layout.CornerRadii(12),
          scalafx.scene.layout.BorderWidths(2)
        )
      )
      children = List(
        pauseButton,
        saveButton,
        backButton,
        learnMoreButton,
        moneyLabel,
        sustainabilityLabel
      )

  def updateStatus(): Unit =
    // Update money and sustainability - gotta keep the player informed!
    moneyLabel.text = f"Money: $$${gameState.currency}%.2f"
    sustainabilityLabel.text =
      f"Sustainability: ${gameState.globalSustainabilityRating}%.1f%%"
    updateSustainabilityColor()

  private def updateSustainabilityColor(): Unit =
    // Colors that help show how good/bad the farm is doing
    val sustainabilityRating = gameState.globalSustainabilityRating
    sustainabilityLabel.textFill =
      if sustainabilityRating >= 80 then Color.LightGreen // Great job!
      else if sustainabilityRating >= 60 then Color.Yellow // not bad
      else if sustainabilityRating >= 40 then Color.Orange // Could be better
      else Color.Red // Oh no!

end FarmMenuBar
