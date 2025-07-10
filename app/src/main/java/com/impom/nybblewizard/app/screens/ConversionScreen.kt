package com.impom.nybblewizard.app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.impom.nybblewizard.app.reusable.TopNavBar
import kotlin.contracts.contract

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
    val context = LocalContext.current
    var selectedImgUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract= ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedImgUri = uri}
    )
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
                var input by remember {mutableStateOf("")}
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {input = it},
                    label = { Text("Enter Something Jesse : ")},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { launcher.launch("image/*")},
                    modifier = Modifier.align(Alignment.End)
                    ) {
                    Text("Cook")
                }
                selectedImgUri?.let {
                    Text("Selected: ${it.lastPathSegment}", maxLines = 1)
                }

            }

            }
        }
        }
    }

