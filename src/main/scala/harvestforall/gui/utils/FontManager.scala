package harvestforall.gui.utils

import scalafx.scene.text.Font

/** Font manager for loading and providing custom fonts throughout the
  * application - handles that retro pixel font we're using
  */
object FontManager:

  private val FONT_PATH = "/font/upheaval/upheavtt.ttf"

  // Load the font once to register it with the system
  private val loadedFont: Option[Font] = loadFontForRegistration()

  // Pre-load different font sizes for better performance (lazy loading ftw)
  lazy val titleFont: Font = loadCustomFont(48)
  lazy val subtitleFont: Font = loadCustomFont(18)
  lazy val headerFont: Font = loadCustomFont(28)
  lazy val subHeaderFont: Font = loadCustomFont(20)
  lazy val buttonFont: Font = loadCustomFont(16)
  lazy val labelFont: Font = loadCustomFont(14)
  lazy val smallFont: Font = loadCustomFont(12)

  /** Load font for registration with the system
    *
    * @return
    *   Option[Font] if successful, None if failed
    */
  private def loadFontForRegistration(): Option[Font] =
    try
      val fontStream = getClass.getResourceAsStream(FONT_PATH)
      if fontStream != null then
        val font = Font.loadFont(
          fontStream,
          12
        ) // Load with default size for registration
        println(s"[FontManager] Successfully loaded font: ${font.getName}")
        Some(font)
      else
        println(s"[FontManager] Warning: Could not find font at $FONT_PATH")
        None
    catch
      case e: Exception =>
        println(
          s"[FontManager] Error loading font for registration: ${e.getMessage}"
        )
        None

  /** Load custom font with specified size
    *
    * @param size
    *   The font size
    * @return
    *   Font instance, fallback to default if loading fails
    */
  private def loadCustomFont(size: Double): Font =
    try
      val fontStream = getClass.getResourceAsStream(FONT_PATH)
      if fontStream != null then Font.loadFont(fontStream, size)
      else
        println(
          s"[FontManager] Warning: Could not find font at $FONT_PATH, using default"
        )
        Font.font("Monospaced", size) // Fallback to monospaced font
    catch
      case e: Exception =>
        println(
          s"[FontManager] Error loading font: ${e.getMessage}, using default"
        )
        Font.font("Monospaced", size) // Fallback to monospaced font

  /** Get custom font with specific size (for cases where pre-loaded sizes
    * aren't sufficient)
    *
    * @param size
    *   The font size
    * @return
    *   Font instance
    */
  def getCustomFont(size: Double): Font = loadCustomFont(size)

  /** Get the CSS font family name for use in CSS styles
    *
    * @return
    *   The font family name
    */
  def getFontFamilyName: String =
    loadedFont.map(_.getName).getOrElse("upheavtt")

  /** Get CSS font family with fallback for use in CSS styles
    *
    * @return
    *   CSS font family string with fallbacks
    */
  def getCSSFontFamily: String =
    val fontName = loadedFont.map(_.getName).getOrElse("upheavtt")
    s"'$fontName', 'Courier New', monospace"

end FontManager
