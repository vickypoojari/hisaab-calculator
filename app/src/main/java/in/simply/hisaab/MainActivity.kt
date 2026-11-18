package `in`.simply.hisaab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import `in`.simply.hisaab.core.designsystem.HisaabTheme

/**
 * Placeholder shell. The walking skeleton (select text -> parser -> result sheet) replaces this.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HisaabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShellPreview() {
    HisaabTheme { Text(stringResource(R.string.app_name)) }
}
