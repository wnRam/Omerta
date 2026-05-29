package uz.angrykitten.omerta.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Role icons (11 total). Composition strategy:
 *   1. **Filled silhouette base** — gives the icon presence at small sizes.
 *   2. **Negative-space details** (eye sockets, badge centers) via [solidEvenOdd].
 *   3. **Slim accent strokes** layered over the base for character.
 *
 * Tinted by the caller — usually a team color for built-ins.
 */
object RoleIcons {

    /** Citizen — fedora-and-coat figure with a thin scarf accent. */
    val Citizen: ImageVector by lazy {
        omertaIcon("Role.Citizen") {
            solid {
                // Hat crown + brim
                moveTo(7f, 4f); lineTo(17f, 4f); lineTo(18f, 9f); lineTo(6f, 9f); close()
                moveTo(4.5f, 9f); lineTo(19.5f, 9f); lineTo(18.5f, 10.5f); lineTo(5.5f, 10.5f); close()
                // Head
                moveTo(14.5f, 13f)
                arcTo(2.5f, 2.5f, 0f, true, true, 9.5f, 13f)
                arcTo(2.5f, 2.5f, 0f, true, true, 14.5f, 13f)
                close()
                // Coat — broad shoulders
                moveTo(4f, 22f); lineTo(5f, 16f); lineTo(8f, 14.5f); lineTo(16f, 14.5f); lineTo(19f, 16f); lineTo(20f, 22f); close()
            }
            accent {
                // Hat band
                moveTo(6.5f, 8.2f); lineTo(17.5f, 8.2f)
                // Lapel
                moveTo(12f, 14.5f); lineTo(12f, 20f)
            }
        }
    }

    /** Mafia — shadowed figure, half-mask covering eyes, hat brim broken. */
    val Mafia: ImageVector by lazy {
        omertaIcon("Role.Mafia") {
            solid {
                // Wide-brimmed fedora
                moveTo(3.5f, 9f); lineTo(20.5f, 9f); lineTo(18.5f, 10f); lineTo(5.5f, 10f); close()
                moveTo(7f, 4.5f); lineTo(17f, 4.5f); lineTo(17f, 9f); lineTo(7f, 9f); close()
                // Head silhouette
                moveTo(15f, 13.5f)
                arcTo(3f, 3f, 0f, true, true, 9f, 13.5f)
                arcTo(3f, 3f, 0f, true, true, 15f, 13.5f)
                close()
                // Mask — solid band over eyes (cuts head in half visually)
                moveTo(7f, 12f); lineTo(17f, 12f); lineTo(17f, 14.2f); lineTo(7f, 14.2f); close()
                // Trench coat with raised collar
                moveTo(3f, 22f); lineTo(5f, 16f); lineTo(8.5f, 14.5f); lineTo(15.5f, 14.5f); lineTo(19f, 16f); lineTo(21f, 22f); close()
            }
            accent {
                // Hat indent
                moveTo(10f, 5.5f); lineTo(14f, 5.5f)
                // Collar V
                moveTo(10f, 15f); lineTo(12f, 17f); lineTo(14f, 15f)
            }
        }
    }

    /** Sheriff — chunky 5-point star with center dot + ring. */
    val Sheriff: ImageVector by lazy {
        omertaIcon("Role.Sheriff") {
            solid {
                // 5-point star
                val cx = 12f
                val cy = 12.2f
                val rOuter = 9.5f
                val rInner = 3.9f
                for (i in 0 until 10) {
                    val angle = Math.toRadians((i * 36 - 90).toDouble())
                    val radius = if (i % 2 == 0) rOuter else rInner
                    val x = (cx + radius * Math.cos(angle)).toFloat()
                    val y = (cy + radius * Math.sin(angle)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            // Negative-space inner ring with center dot
            solidEvenOdd {
                moveTo(15f, 12.2f)
                arcTo(3f, 3f, 0f, true, true, 9f, 12.2f)
                arcTo(3f, 3f, 0f, true, true, 15f, 12.2f)
                close()
                moveTo(13.2f, 12.2f)
                arcTo(1.2f, 1.2f, 0f, true, true, 10.8f, 12.2f)
                arcTo(1.2f, 1.2f, 0f, true, true, 13.2f, 12.2f)
                close()
            }
            // Chip detail on top-right point (vintage broken edge)
            accent {
                moveTo(17f, 7f); lineTo(18.6f, 8.6f)
            }
        }
    }

    /** Doctor — vintage bag with cross + clasp + handle. */
    val Doctor: ImageVector by lazy {
        omertaIcon("Role.Doctor") {
            solid {
                // Bag body — trapezoidal noir doctor's bag
                moveTo(4f, 10.5f); lineTo(20f, 10.5f); lineTo(19f, 21f); lineTo(5f, 21f); close()
                // Bag top lip
                moveTo(3.5f, 10.5f); lineTo(20.5f, 10.5f); lineTo(20.5f, 11.5f); lineTo(3.5f, 11.5f); close()
                // Handle — curved
                moveTo(9f, 10.5f)
                curveTo(9f, 6f, 15f, 6f, 15f, 10.5f)
                lineTo(13.4f, 10.5f)
                curveTo(13.4f, 8.2f, 10.6f, 8.2f, 10.6f, 10.5f)
                close()
            }
            // Cross — drawn as negative space (so it 'pops' from any tint)
            solidEvenOdd {
                // Outer rounded rect (cross-bearing badge)
                moveTo(8f, 13.5f); lineTo(16f, 13.5f); lineTo(16f, 19.5f); lineTo(8f, 19.5f); close()
                // Cross cutout — vertical bar
                moveTo(11.4f, 14.6f); lineTo(12.6f, 14.6f); lineTo(12.6f, 18.4f); lineTo(11.4f, 18.4f); close()
                // Cross cutout — horizontal bar
                moveTo(9.2f, 16f); lineTo(14.8f, 16f); lineTo(14.8f, 17f); lineTo(9.2f, 17f); close()
            }
        }
    }

    /** Don — three-tier crown + throne + draped sash. */
    val Don: ImageVector by lazy {
        omertaIcon("Role.Don") {
            solid {
                // Tall throne back
                moveTo(7f, 9f); lineTo(17f, 9f); lineTo(17.5f, 15.5f); lineTo(6.5f, 15.5f); close()
                // Seat + base
                moveTo(5f, 15.5f); lineTo(19f, 15.5f); lineTo(19f, 18f); lineTo(5f, 18f); close()
                moveTo(5.5f, 18f); lineTo(8f, 18f); lineTo(8f, 22f); lineTo(5.5f, 22f); close()
                moveTo(16f, 18f); lineTo(18.5f, 18f); lineTo(18.5f, 22f); lineTo(16f, 22f); close()
                // Crown — three peaks with gems
                moveTo(6f, 8f); lineTo(7.5f, 3.5f); lineTo(9.5f, 6.5f)
                lineTo(12f, 2.5f); lineTo(14.5f, 6.5f); lineTo(16.5f, 3.5f); lineTo(18f, 8f); close()
                // Crown base bar
                moveTo(6.5f, 8f); lineTo(17.5f, 8f); lineTo(17.5f, 9f); lineTo(6.5f, 9f); close()
            }
            // Center gem on crown (negative space)
            solidEvenOdd {
                moveTo(13f, 5.5f)
                arcTo(1f, 1f, 0f, true, true, 11f, 5.5f)
                arcTo(1f, 1f, 0f, true, true, 13f, 5.5f)
                close()
                // Throne center monogram bar
                moveTo(11f, 10.5f); lineTo(13f, 10.5f); lineTo(13f, 14.5f); lineTo(11f, 14.5f); close()
            }
        }
    }

    /** Maniac — cracked porcelain mask with a single hollow eye. */
    val Maniac: ImageVector by lazy {
        omertaIcon("Role.Maniac") {
            solid {
                // Mask shape — broader top, narrowing to a chin
                moveTo(5.5f, 5f); lineTo(18.5f, 5f); lineTo(20f, 12f); lineTo(17f, 19f); lineTo(12f, 22f); lineTo(7f, 19f); lineTo(4f, 12f); close()
            }
            // Two eye holes — one solid 'living' eye, one hollow
            solidEvenOdd {
                // Hollow eye (right)
                moveTo(17f, 11.5f)
                arcTo(2f, 2f, 0f, true, true, 13f, 11.5f)
                arcTo(2f, 2f, 0f, true, true, 17f, 11.5f)
                close()
            }
            accent {
                // The 'living' eye (left) — drawn as a fine ring with pupil
                moveTo(11f, 11.5f)
                arcTo(2f, 2f, 0f, true, true, 7f, 11.5f)
                arcTo(2f, 2f, 0f, true, true, 11f, 11.5f)
                close()
                // Crack — jagged line crossing the mask
                moveTo(5.5f, 7f); lineTo(8.5f, 10f); lineTo(7f, 13.5f); lineTo(11f, 16f); lineTo(9.5f, 19f)
                // Mouth slit
                moveTo(10f, 17.5f); lineTo(14f, 17.5f)
            }
            solidEvenOdd {
                // Pupil of living eye
                moveTo(9.7f, 11.5f)
                arcTo(0.7f, 0.7f, 0f, true, true, 8.3f, 11.5f)
                arcTo(0.7f, 0.7f, 0f, true, true, 9.7f, 11.5f)
                close()
            }
        }
    }

    /** Detective — oversized magnifying glass with eye behind it. */
    val Detective: ImageVector by lazy {
        omertaIcon("Role.Detective") {
            solid {
                // Hat brim — fedora silhouette top-right
                moveTo(13f, 3.5f); lineTo(22f, 3.5f); lineTo(20.5f, 6f); lineTo(14.5f, 6f); close()
                moveTo(15f, 1f); lineTo(20f, 1f); lineTo(20.5f, 4f); lineTo(14.5f, 4f); close()
            }
            outline(strokeWidth = 2.4f) {
                // Magnifier disc
                moveTo(14f, 12f)
                arcTo(6f, 6f, 0f, true, true, 2f, 12f)
                arcTo(6f, 6f, 0f, true, true, 14f, 12f)
                close()
                // Handle
                moveTo(13f, 16.5f); lineTo(20f, 22f)
            }
            solid {
                // Eye inside the disc
                moveTo(8f, 12f)
                arcTo(2f, 2f, 0f, true, true, 4f, 12f)
                arcTo(2f, 2f, 0f, true, true, 8f, 12f)
                close()
            }
            // Highlight gleam on disc
            accent {
                moveTo(10f, 8f); lineTo(11.5f, 9f)
            }
        }
    }

    /** Bodyguard — heater shield with bold V-cut + clenched fist sigil. */
    val Bodyguard: ImageVector by lazy {
        omertaIcon("Role.Bodyguard") {
            solid {
                // Shield outline
                moveTo(12f, 2.5f); lineTo(21f, 5.5f); lineTo(21f, 13.5f)
                curveTo(21f, 18f, 17f, 21f, 12f, 22.5f)
                curveTo(7f, 21f, 3f, 18f, 3f, 13.5f)
                lineTo(3f, 5.5f); close()
            }
            // Fist sigil (negative space)
            solidEvenOdd {
                moveTo(8.5f, 10.5f); lineTo(15.5f, 10.5f); lineTo(15.5f, 16.5f); lineTo(8.5f, 16.5f); close()
                // Knuckle ridges
                moveTo(9.2f, 11f); lineTo(9.2f, 12.5f); lineTo(10.6f, 12.5f); lineTo(10.6f, 11f); close()
                moveTo(11.3f, 11f); lineTo(11.3f, 12.5f); lineTo(12.7f, 12.5f); lineTo(12.7f, 11f); close()
                moveTo(13.4f, 11f); lineTo(13.4f, 12.5f); lineTo(14.8f, 12.5f); lineTo(14.8f, 11f); close()
                // Thumb knob
                moveTo(15.5f, 12.5f); lineTo(16.5f, 12.5f); lineTo(16.5f, 14.5f); lineTo(15.5f, 14.5f); close()
            }
            accent {
                // Chevron at top of shield
                moveTo(8f, 5.5f); lineTo(12f, 7f); lineTo(16f, 5.5f)
            }
        }
    }

    /** Courtesan — venetian half-mask with feather + lace tassels. */
    val Prostitute: ImageVector by lazy {
        omertaIcon("Role.Prostitute") {
            solid {
                // Mask body — twin lobes
                moveTo(2f, 9f)
                curveTo(2f, 5.5f, 8f, 5.5f, 12f, 9f)
                curveTo(16f, 5.5f, 22f, 5.5f, 22f, 9f)
                curveTo(22f, 13.5f, 18f, 16.5f, 14f, 16.5f)
                lineTo(10f, 16.5f)
                curveTo(6f, 16.5f, 2f, 13.5f, 2f, 9f)
                close()
                // Feather plume on right
                moveTo(19f, 5.5f); lineTo(22f, 1f); lineTo(20.5f, 6.5f); close()
                // Decorative tassel (left)
                moveTo(5f, 14.5f); lineTo(4f, 21f); lineTo(6f, 17f); close()
                moveTo(19f, 14.5f); lineTo(20f, 21f); lineTo(18f, 17f); close()
            }
            // Eye holes — almond-shaped negative space
            solidEvenOdd {
                moveTo(10f, 9.5f)
                curveTo(10f, 11.5f, 6f, 11.5f, 6f, 9.5f)
                curveTo(6f, 7.5f, 10f, 7.5f, 10f, 9.5f)
                close()
                moveTo(18f, 9.5f)
                curveTo(18f, 11.5f, 14f, 11.5f, 14f, 9.5f)
                curveTo(14f, 7.5f, 18f, 7.5f, 18f, 9.5f)
                close()
            }
            // Lace pattern along bottom
            accent {
                moveTo(7f, 14f); lineTo(8f, 15f); lineTo(9f, 14f); lineTo(10f, 15f); lineTo(11f, 14f); lineTo(12f, 15f); lineTo(13f, 14f); lineTo(14f, 15f); lineTo(15f, 14f); lineTo(16f, 15f); lineTo(17f, 14f)
            }
        }
    }

    /** Mayor — laurel wreath atop crested podium, sash-draped. */
    val Mayor: ImageVector by lazy {
        omertaIcon("Role.Mayor") {
            // Laurel wreath
            outline(strokeWidth = 1.8f) {
                moveTo(6f, 9.5f)
                curveTo(6f, 4.5f, 12f, 2.5f, 12f, 2.5f)
                curveTo(12f, 2.5f, 18f, 4.5f, 18f, 9.5f)
                // Leaves (left side)
                moveTo(7.5f, 7f); lineTo(9f, 8.5f)
                moveTo(8.5f, 5f); lineTo(10f, 6.5f)
                moveTo(7f, 9f); lineTo(8.5f, 10f)
                // Leaves (right side)
                moveTo(16.5f, 7f); lineTo(15f, 8.5f)
                moveTo(15.5f, 5f); lineTo(14f, 6.5f)
                moveTo(17f, 9f); lineTo(15.5f, 10f)
            }
            solid {
                // Podium cap + body
                moveTo(4.5f, 11f); lineTo(19.5f, 11f); lineTo(19.5f, 13f); lineTo(4.5f, 13f); close()
                moveTo(7f, 13f); lineTo(17f, 13f); lineTo(17f, 22f); lineTo(7f, 22f); close()
            }
            // Crest carved into podium (negative)
            solidEvenOdd {
                moveTo(10f, 15f); lineTo(14f, 15f); lineTo(14f, 19f); lineTo(10f, 19f); close()
                // Inner star
                moveTo(12f, 15.4f); lineTo(12.6f, 16.6f); lineTo(13.8f, 16.6f); lineTo(12.8f, 17.4f); lineTo(13.2f, 18.6f); lineTo(12f, 17.9f); lineTo(10.8f, 18.6f); lineTo(11.2f, 17.4f); lineTo(10.2f, 16.6f); lineTo(11.4f, 16.6f); close()
            }
        }
    }

    /** Custom — ornate baroque frame with looping question scroll. */
    val CustomRole: ImageVector by lazy {
        omertaIcon("Role.Custom") {
            outline(strokeWidth = 1.8f) {
                // Outer ornate frame
                moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 21f); lineTo(3f, 21f); close()
                // Inner frame
                moveTo(5f, 7f); lineTo(19f, 7f); lineTo(19f, 19f); lineTo(5f, 19f); close()
                // Corner flourishes
                moveTo(3f, 8f); lineTo(6f, 5f)
                moveTo(21f, 8f); lineTo(18f, 5f)
                moveTo(3f, 18f); lineTo(6f, 21f)
                moveTo(21f, 18f); lineTo(18f, 21f)
            }
            solid {
                // Question mark — bold, slightly serif
                moveTo(9.5f, 10.5f)
                curveTo(9.5f, 8f, 14.5f, 8f, 14.5f, 10.5f)
                curveTo(14.5f, 12.5f, 12f, 12.5f, 12f, 14.5f)
                lineTo(11f, 14.5f)
                curveTo(11f, 11.5f, 13.5f, 12f, 13.5f, 10.5f)
                curveTo(13.5f, 9f, 10.5f, 9f, 10.5f, 10.5f)
                close()
                // Dot of the question mark
                moveTo(12.7f, 16.5f)
                arcTo(0.75f, 0.75f, 0f, true, true, 11.2f, 16.5f)
                arcTo(0.75f, 0.75f, 0f, true, true, 12.7f, 16.5f)
                close()
            }
        }
    }
}
