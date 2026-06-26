package com.biodataai.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biodataai.app.R

@Composable
fun OfflineStateBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE53935))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.offline_banner),
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}
