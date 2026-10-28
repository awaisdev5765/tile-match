/*
 * Copyright (C) 2026  InfraRed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see:
 * https://www.gnu.org/licenses/
 */

package infrared.tilematch.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Modifier.darkenBackground(): Modifier = this.background(Color.Black.copy(alpha = 0.75f))


@Composable
fun BaseMenu(
    title: String,
    isNightMode: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().darkenBackground(),
        contentAlignment = Alignment.Center
    ) {
        val surfaceBrush = if (isNightMode) {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF3A3A3A), Color(0xFF2E2E2E))
            )
        }

        val borderColor = if (isNightMode) {
            Color(0x99333333)
        } else {
            Color(0x99FFFFFF)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            border = BorderStroke(2.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .background(surfaceBrush)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                content()
            }
        }
    }
}