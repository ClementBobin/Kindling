package dev.kindling.compose.preview

import androidx.compose.ui.tooling.preview.Preview

`@Preview`(name = "Light — phone", showBackground = true, widthDp = 390)
`@Preview`(name = "Dark — phone",  showBackground = true, widthDp = 390, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
`@Preview`(name = "Light — tablet", showBackground = true, widthDp = 800)
`@Preview`(name = "Dark — tablet", showBackground = true, widthDp = 800, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
annotation class KPreview
