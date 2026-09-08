package com.module.notelycompose.notes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Named corner radii, replacing the ungoverned mix of 4/8/12/16/20/28/32/48dp across screens. */
object AppRadii {
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 28.dp
    val pill = RoundedCornerShape(50)

    val shapeXs = RoundedCornerShape(xs)
    val shapeSm = RoundedCornerShape(sm)
    val shapeMd = RoundedCornerShape(md)
    val shapeLg = RoundedCornerShape(lg)
    val shapeXl = RoundedCornerShape(xl)
}

val AppShapes = Shapes(
    extraSmall = AppRadii.shapeXs,
    small = AppRadii.shapeSm,
    medium = AppRadii.shapeMd,
    large = AppRadii.shapeLg,
    extraLarge = AppRadii.shapeXl
)
