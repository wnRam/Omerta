package uz.angrykitten.omerta.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The full UI icon set. All hand-built — none come from `Icons.Default.*`.
 * Mix of outline (chrome) and solid (statement) styles.
 */
object OmertaIcons {

    /** House silhouette with peaked roof + door cutout + window dot. */
    val Home: ImageVector by lazy {
        omertaIcon("Home") {
            solid {
                moveTo(3.5f, 11f); lineTo(12f, 3.5f); lineTo(20.5f, 11f); lineTo(20.5f, 21f); lineTo(3.5f, 21f); close()
            }
            solidEvenOdd {
                // Door cutout
                moveTo(10f, 21f); lineTo(10f, 14f); lineTo(14f, 14f); lineTo(14f, 21f); close()
                // Window cutout
                moveTo(7.5f, 13f)
                arcTo(0.8f, 0.8f, 0f, true, true, 5.9f, 13f)
                arcTo(0.8f, 0.8f, 0f, true, true, 7.5f, 13f)
                close()
                moveTo(18.1f, 13f)
                arcTo(0.8f, 0.8f, 0f, true, true, 16.5f, 13f)
                arcTo(0.8f, 0.8f, 0f, true, true, 18.1f, 13f)
                close()
            }
        }
    }

    /** Custom gear — 6 teeth + bold center hole. */
    val Settings: ImageVector by lazy {
        omertaIcon("Settings") {
            solid {
                val cx = 12f; val cy = 12f; val r = 8.5f
                for (i in 0 until 12) {
                    val angle = Math.toRadians((i * 30 - 90).toDouble())
                    val radius = if (i % 2 == 0) r else r - 2.2f
                    val x = (cx + radius * Math.cos(angle)).toFloat()
                    val y = (cy + radius * Math.sin(angle)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            // Center hole (negative)
            solidEvenOdd {
                moveTo(15.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, true, true, 8.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, true, true, 15.5f, 12f)
                close()
            }
        }
    }

    val Back: ImageVector by lazy {
        omertaIcon("Back", autoMirror = true) {
            outline(strokeWidth = 2.4f) {
                moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        omertaIcon("ChevronRight", autoMirror = true) {
            outline(strokeWidth = 2.4f) {
                moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
            }
        }
    }

    val Close: ImageVector by lazy {
        omertaIcon("Close") {
            outline(strokeWidth = 2.4f) {
                moveTo(6f, 6f); lineTo(18f, 18f)
                moveTo(18f, 6f); lineTo(6f, 18f)
            }
        }
    }

    val Add: ImageVector by lazy {
        omertaIcon("Add") {
            outline(strokeWidth = 2.4f) {
                moveTo(12f, 5f); lineTo(12f, 19f)
                moveTo(5f, 12f); lineTo(19f, 12f)
            }
        }
    }

    val Remove: ImageVector by lazy {
        omertaIcon("Remove") {
            outline(strokeWidth = 2.4f) {
                moveTo(5f, 12f); lineTo(19f, 12f)
            }
        }
    }

    val Play: ImageVector by lazy {
        omertaIcon("Play") {
            solid {
                moveTo(6.5f, 4.5f); lineTo(19.5f, 12f); lineTo(6.5f, 19.5f); close()
            }
        }
    }

    val Pause: ImageVector by lazy {
        omertaIcon("Pause") {
            solid {
                moveTo(6.5f, 5f); lineTo(10f, 5f); lineTo(10f, 19f); lineTo(6.5f, 19f); close()
                moveTo(14f, 5f); lineTo(17.5f, 5f); lineTo(17.5f, 19f); lineTo(14f, 19f); close()
            }
        }
    }

    val Skip: ImageVector by lazy {
        omertaIcon("Skip", autoMirror = true) {
            solid {
                moveTo(4.5f, 4.5f); lineTo(15f, 12f); lineTo(4.5f, 19.5f); close()
                moveTo(16f, 5f); lineTo(19.5f, 5f); lineTo(19.5f, 19f); lineTo(16f, 19f); close()
            }
        }
    }

    /** QR scan — bold corner brackets + scan line. */
    val QrScan: ImageVector by lazy {
        omertaIcon("QrScan") {
            outline(strokeWidth = 2.4f) {
                moveTo(4f, 9f); lineTo(4f, 4f); lineTo(9f, 4f)
                moveTo(15f, 4f); lineTo(20f, 4f); lineTo(20f, 9f)
                moveTo(20f, 15f); lineTo(20f, 20f); lineTo(15f, 20f)
                moveTo(9f, 20f); lineTo(4f, 20f); lineTo(4f, 15f)
                moveTo(6f, 12f); lineTo(18f, 12f)
            }
        }
    }

    val Share: ImageVector by lazy {
        omertaIcon("Share") {
            solid {
                // Three dots connected by lines — classic share node
                // top-right
                moveTo(19.5f, 5f)
                arcTo(2.2f, 2.2f, 0f, true, true, 15.1f, 5f)
                arcTo(2.2f, 2.2f, 0f, true, true, 19.5f, 5f)
                close()
                // middle-left
                moveTo(8.5f, 12f)
                arcTo(2.2f, 2.2f, 0f, true, true, 4.1f, 12f)
                arcTo(2.2f, 2.2f, 0f, true, true, 8.5f, 12f)
                close()
                // bottom-right
                moveTo(19.5f, 19f)
                arcTo(2.2f, 2.2f, 0f, true, true, 15.1f, 19f)
                arcTo(2.2f, 2.2f, 0f, true, true, 19.5f, 19f)
                close()
            }
            accent {
                moveTo(8f, 11f); lineTo(15f, 6.5f)
                moveTo(8f, 13f); lineTo(15f, 17.5f)
            }
        }
    }

    /** Clock face — solid with hands. */
    val Timer: ImageVector by lazy {
        omertaIcon("Timer") {
            solid {
                // Disc
                moveTo(21f, 13f)
                arcTo(9f, 9f, 0f, true, true, 3f, 13f)
                arcTo(9f, 9f, 0f, true, true, 21f, 13f)
                close()
                // Stem
                moveTo(10f, 2.5f); lineTo(14f, 2.5f); lineTo(14f, 4.5f); lineTo(10f, 4.5f); close()
            }
            // Hands — negative space
            solidEvenOdd {
                moveTo(11.4f, 8f); lineTo(12.6f, 8f); lineTo(12.6f, 13.5f); lineTo(11.4f, 13.5f); close()
                moveTo(12f, 13f); lineTo(15.5f, 14.5f); lineTo(15f, 15.5f); lineTo(11.5f, 14f); close()
                // Center pivot dot
                moveTo(12.7f, 13f)
                arcTo(0.7f, 0.7f, 0f, true, true, 11.3f, 13f)
                arcTo(0.7f, 0.7f, 0f, true, true, 12.7f, 13f)
                close()
            }
        }
    }

    /** Bold crescent moon. */
    val Night: ImageVector by lazy {
        omertaIcon("Night") {
            solid {
                moveTo(20f, 14f)
                arcTo(9f, 9f, 0f, true, true, 10f, 4f)
                arcTo(7f, 7f, 0f, false, false, 20f, 14f)
                close()
            }
            // Tiny stars
            solid {
                moveTo(19f, 6f); lineTo(19.5f, 5f); lineTo(20f, 6f); lineTo(21f, 6.5f); lineTo(20f, 7f); lineTo(19.5f, 8f); lineTo(19f, 7f); lineTo(18f, 6.5f); close()
                moveTo(15f, 8f); lineTo(15.4f, 7.4f); lineTo(15.8f, 8f); lineTo(16.4f, 8.4f); lineTo(15.8f, 8.8f); lineTo(15.4f, 9.4f); lineTo(15f, 8.8f); lineTo(14.4f, 8.4f); close()
            }
        }
    }

    /** Sun with 8 rays — bold disc + thick rays. */
    val Day: ImageVector by lazy {
        omertaIcon("Day") {
            solid {
                // Disc
                moveTo(17f, 12f)
                arcTo(5f, 5f, 0f, true, true, 7f, 12f)
                arcTo(5f, 5f, 0f, true, true, 17f, 12f)
                close()
            }
            outline(strokeWidth = 2.6f) {
                // Cardinal rays
                moveTo(12f, 2f); lineTo(12f, 4.5f)
                moveTo(12f, 19.5f); lineTo(12f, 22f)
                moveTo(2f, 12f); lineTo(4.5f, 12f)
                moveTo(19.5f, 12f); lineTo(22f, 12f)
                // Diagonal rays
                moveTo(4.5f, 4.5f); lineTo(6.5f, 6.5f)
                moveTo(17.5f, 17.5f); lineTo(19.5f, 19.5f)
                moveTo(19.5f, 4.5f); lineTo(17.5f, 6.5f)
                moveTo(4.5f, 19.5f); lineTo(6.5f, 17.5f)
            }
        }
    }

    /** Ballot box with check mark — solid + negative tick. */
    val Vote: ImageVector by lazy {
        omertaIcon("Vote") {
            solid {
                // Ballot above box
                moveTo(7f, 3f); lineTo(17f, 3f); lineTo(17f, 11f); lineTo(7f, 11f); close()
                // Box
                moveTo(3.5f, 11f); lineTo(20.5f, 11f); lineTo(20.5f, 21f); lineTo(3.5f, 21f); close()
            }
            solidEvenOdd {
                // Slot in box
                moveTo(8.5f, 11.5f); lineTo(15.5f, 11.5f); lineTo(15.5f, 12.5f); lineTo(8.5f, 12.5f); close()
                // Check on ballot — negative space
                moveTo(9.5f, 7f); lineTo(11f, 8.5f); lineTo(14.5f, 5f); lineTo(15.5f, 6f); lineTo(11f, 10.5f); lineTo(8.5f, 8f); close()
            }
        }
    }

    /** Crown — 3 peaks with 3 gems. Bigger, more regal. */
    val Crown: ImageVector by lazy {
        omertaIcon("Crown") {
            solid {
                // Crown body — three peaks
                moveTo(3f, 18f)
                lineTo(4.5f, 7f)
                lineTo(8.5f, 11.5f)
                lineTo(12f, 5f)
                lineTo(15.5f, 11.5f)
                lineTo(19.5f, 7f)
                lineTo(21f, 18f)
                close()
                // Base bar
                moveTo(3.5f, 18.5f); lineTo(20.5f, 18.5f); lineTo(20.5f, 21f); lineTo(3.5f, 21f); close()
            }
            // Three gems (negative space)
            solidEvenOdd {
                moveTo(5.5f, 14f)
                arcTo(0.9f, 0.9f, 0f, true, true, 3.7f, 14f)
                arcTo(0.9f, 0.9f, 0f, true, true, 5.5f, 14f)
                close()
                moveTo(13f, 13f)
                arcTo(1.1f, 1.1f, 0f, true, true, 10.8f, 13f)
                arcTo(1.1f, 1.1f, 0f, true, true, 13f, 13f)
                close()
                moveTo(20.3f, 14f)
                arcTo(0.9f, 0.9f, 0f, true, true, 18.5f, 14f)
                arcTo(0.9f, 0.9f, 0f, true, true, 20.3f, 14f)
                close()
            }
        }
    }

    /** Eye — solid almond + iris + pupil dot. */
    val Eye: ImageVector by lazy {
        omertaIcon("Eye") {
            solid {
                moveTo(2f, 12f)
                curveTo(5f, 5.5f, 9f, 4.5f, 12f, 4.5f)
                curveTo(15f, 4.5f, 19f, 5.5f, 22f, 12f)
                curveTo(19f, 18.5f, 15f, 19.5f, 12f, 19.5f)
                curveTo(9f, 19.5f, 5f, 18.5f, 2f, 12f)
                close()
            }
            solidEvenOdd {
                // Iris ring
                moveTo(15.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, true, true, 8.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, true, true, 15.5f, 12f)
                close()
                // Pupil dot
                moveTo(13.2f, 12f)
                arcTo(1.2f, 1.2f, 0f, true, true, 10.8f, 12f)
                arcTo(1.2f, 1.2f, 0f, true, true, 13.2f, 12f)
                close()
            }
        }
    }

    /** Padlock — solid body, thick shackle, keyhole cutout. */
    val Lock: ImageVector by lazy {
        omertaIcon("Lock") {
            solid {
                // Body
                moveTo(4f, 10.5f); lineTo(20f, 10.5f); lineTo(20f, 21.5f); lineTo(4f, 21.5f); close()
            }
            outline(strokeWidth = 2.4f) {
                // Shackle
                moveTo(7.5f, 10.5f); lineTo(7.5f, 7f)
                curveTo(7.5f, 4.5f, 9.5f, 2.5f, 12f, 2.5f)
                curveTo(14.5f, 2.5f, 16.5f, 4.5f, 16.5f, 7f)
                lineTo(16.5f, 10.5f)
            }
            // Keyhole — negative
            solidEvenOdd {
                moveTo(13.4f, 15f)
                arcTo(1.4f, 1.4f, 0f, true, true, 10.6f, 15f)
                arcTo(1.4f, 1.4f, 0f, true, true, 13.4f, 15f)
                close()
                moveTo(11.2f, 15.5f); lineTo(12.8f, 15.5f); lineTo(12.6f, 19f); lineTo(11.4f, 19f); close()
            }
        }
    }

    /** Globe — concentric rings + meridian. */
    val Language: ImageVector by lazy {
        omertaIcon("Language") {
            solid {
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, true, true, 3f, 12f)
                arcTo(9f, 9f, 0f, true, true, 21f, 12f)
                close()
            }
            solidEvenOdd {
                // Equator + latitudes
                moveTo(3.5f, 11.5f); lineTo(20.5f, 11.5f); lineTo(20.5f, 12.5f); lineTo(3.5f, 12.5f); close()
                // Vertical meridian
                moveTo(11.5f, 3.5f); lineTo(12.5f, 3.5f); lineTo(12.5f, 20.5f); lineTo(11.5f, 20.5f); close()
                // Top latitude
                moveTo(5.5f, 7.5f); lineTo(18.5f, 7.5f); lineTo(18.5f, 8.3f); lineTo(5.5f, 8.3f); close()
                // Bottom latitude
                moveTo(5.5f, 15.5f); lineTo(18.5f, 15.5f); lineTo(18.5f, 16.3f); lineTo(5.5f, 16.3f); close()
            }
        }
    }

    /** Bold checkmark. */
    val Checkmark: ImageVector by lazy {
        omertaIcon("Checkmark") {
            outline(strokeWidth = 3f) {
                moveTo(4.5f, 12.5f); lineTo(10f, 18f); lineTo(19.5f, 6.5f)
            }
        }
    }

    /** Warning triangle — solid with negative exclamation. */
    val Warning: ImageVector by lazy {
        omertaIcon("Warning") {
            solid {
                moveTo(12f, 2.5f); lineTo(22.5f, 21.5f); lineTo(1.5f, 21.5f); close()
            }
            solidEvenOdd {
                moveTo(11f, 9f); lineTo(13f, 9f); lineTo(13f, 15f); lineTo(11f, 15f); close()
                moveTo(13.1f, 17.6f)
                arcTo(1.1f, 1.1f, 0f, true, true, 10.9f, 17.6f)
                arcTo(1.1f, 1.1f, 0f, true, true, 13.1f, 17.6f)
                close()
            }
        }
    }

    /** Bold doorway. */
    val Door: ImageVector by lazy {
        omertaIcon("Door") {
            solid {
                // Door frame
                moveTo(5f, 2.5f); lineTo(19f, 2.5f); lineTo(19f, 21.5f); lineTo(5f, 21.5f); close()
            }
            // Door panel inset (negative space)
            solidEvenOdd {
                moveTo(7.5f, 4.5f); lineTo(16.5f, 4.5f); lineTo(16.5f, 19.5f); lineTo(7.5f, 19.5f); close()
                // Panel divider
                moveTo(7.5f, 11.5f); lineTo(16.5f, 11.5f); lineTo(16.5f, 12.5f); lineTo(7.5f, 12.5f); close()
            }
            // Doorknob
            solid {
                moveTo(15f, 14f)
                arcTo(0.7f, 0.7f, 0f, true, true, 13.6f, 14f)
                arcTo(0.7f, 0.7f, 0f, true, true, 15f, 14f)
                close()
            }
        }
    }

    /** Open book — solid pages with spine + lines. */
    val Book: ImageVector by lazy {
        omertaIcon("Book") {
            solid {
                // Left page
                moveTo(2.5f, 5f)
                curveTo(6.5f, 4f, 11f, 5f, 12f, 7f)
                lineTo(12f, 21f)
                curveTo(11f, 19f, 6.5f, 18f, 2.5f, 19f)
                close()
                // Right page
                moveTo(21.5f, 5f)
                curveTo(17.5f, 4f, 13f, 5f, 12f, 7f)
                lineTo(12f, 21f)
                curveTo(13f, 19f, 17.5f, 18f, 21.5f, 19f)
                close()
            }
            // Text lines (negative)
            solidEvenOdd {
                moveTo(4f, 9f); lineTo(10f, 9.5f); lineTo(10f, 10.3f); lineTo(4f, 9.8f); close()
                moveTo(4f, 12f); lineTo(10f, 12.5f); lineTo(10f, 13.3f); lineTo(4f, 12.8f); close()
                moveTo(4f, 15f); lineTo(10f, 15.5f); lineTo(10f, 16.3f); lineTo(4f, 15.8f); close()
                moveTo(14f, 9.5f); lineTo(20f, 9f); lineTo(20f, 9.8f); lineTo(14f, 10.3f); close()
                moveTo(14f, 12.5f); lineTo(20f, 12f); lineTo(20f, 12.8f); lineTo(14f, 13.3f); close()
                moveTo(14f, 15.5f); lineTo(20f, 15f); lineTo(20f, 15.8f); lineTo(14f, 16.3f); close()
            }
        }
    }

    /** Copy — two overlapping rounded rects. */
    val Copy: ImageVector by lazy {
        omertaIcon("Copy") {
            solid {
                moveTo(8f, 4f); lineTo(18f, 4f); lineTo(18f, 16f); lineTo(8f, 16f); close()
            }
            solidEvenOdd {
                moveTo(10f, 6f); lineTo(16f, 6f); lineTo(16f, 14f); lineTo(10f, 14f); close()
            }
            outline(strokeWidth = 2f) {
                moveTo(6f, 8f); lineTo(6f, 20f); lineTo(16f, 20f)
            }
        }
    }

    /** Trash — bold can with handle + 2 stripes. */
    val Trash: ImageVector by lazy {
        omertaIcon("Trash") {
            solid {
                // Lid
                moveTo(3f, 6.5f); lineTo(21f, 6.5f); lineTo(21f, 8f); lineTo(3f, 8f); close()
                // Handle
                moveTo(9f, 3.5f); lineTo(15f, 3.5f); lineTo(15f, 5.5f); lineTo(9f, 5.5f); close()
                // Body
                moveTo(5.5f, 8f); lineTo(18.5f, 8f); lineTo(17.5f, 21.5f); lineTo(6.5f, 21.5f); close()
            }
            // Stripes (negative)
            solidEvenOdd {
                moveTo(9.5f, 10.5f); lineTo(10.5f, 10.5f); lineTo(10.5f, 19f); lineTo(9.5f, 19f); close()
                moveTo(13.5f, 10.5f); lineTo(14.5f, 10.5f); lineTo(14.5f, 19f); lineTo(13.5f, 19f); close()
            }
        }
    }

    /** Refresh — bold spiral arrow. */
    val Refresh: ImageVector by lazy {
        omertaIcon("Refresh") {
            outline(strokeWidth = 2.4f) {
                moveTo(20f, 12f)
                arcTo(8f, 8f, 0f, true, true, 6f, 7f)
            }
            solid {
                // Arrow head
                moveTo(2.5f, 3f); lineTo(8f, 4f); lineTo(6.5f, 9f); close()
            }
        }
    }
}
