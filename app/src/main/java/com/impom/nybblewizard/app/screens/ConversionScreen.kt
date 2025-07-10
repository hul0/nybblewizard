package com.impom.nybblewizard.app.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.impom.nybblewizard.app.reusable.TopNavBar

/**
 * A blank Conversion Screen.
 *
 * This composable provides an empty canvas for building the file conversion UI.
 * You can add your Jetpack Compose UI elements inside this function.
 *
 * @param onBackClick A lambda function to be invoked when a back navigation action is requested.
 * This can be used to navigate back in your application's navigation graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showSystemUi = true)
fun ConversionScreen(
    onBackClick: () -> Unit = {}
) {
    MaterialTheme{
        Scaffold (
            topBar = { TopAppBar(
                title = {Text("Conversion")},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")

                    }
                }
            ) }
        ){
            innerPadding -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ){
                // Start Of Our U.I YESSSS I DID IT
        }
        }
    }
}
