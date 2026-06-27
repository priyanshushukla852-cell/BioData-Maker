package com.biodataai.app.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biodataai.app.template.BiodataTypeface
import com.biodataai.app.template.TemplateBlock

/**
 * On-screen rendered preview of a biodata. Consumes the exact same [TemplateBlock] list the PDF
 * engine draws, so the preview is a faithful representation of the exported document rather than a
 * separate text dump. All text uses the bundled Devanagari [FontFamily] so Hindi previews render
 * correctly (the system font would show tofu for some conjuncts).
 */
@Composable
fun BiodataDocumentView(
    blocks: List<TemplateBlock>,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = BiodataTypeface.composeFamily(LocalContext.current)
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp)) {
        blocks.forEach { block ->
            when (block) {
                is TemplateBlock.Title -> Text(
                    text = block.text,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                is TemplateBlock.Section -> Text(
                    text = block.text,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                is TemplateBlock.Field -> Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${block.label}:",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.width(120.dp)
                    )
                    Text(
                        text = block.value,
                        fontFamily = fontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                is TemplateBlock.Paragraph -> Text(
                    text = block.text,
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )

                TemplateBlock.Divider -> HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                is TemplateBlock.Gap -> Spacer(Modifier.height((block.points / 2).dp))
            }
        }
    }
}
