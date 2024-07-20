package com.ko2ic.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ko2ic.sample.ui.theme.SpikeonetimeeventsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpikeonetimeeventsTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "firstPage") {
                    composable("firstPage") {
                        val viewModel = viewModel<MainViewModel>()
                        ObserveAsEvents(viewModel.navigationChannelFlow) { event ->
                            when (event) {
                                is MainViewModel.TransitionEvent.OnClickNextPage -> {
                                    navController.navigate("nextPage")
                                }
                            }
                        }

                        ObserveAsEvents(viewModel.navigationSharedFlow) { event ->
                            when (event) {
                                is MainViewModel.TransitionEvent.OnClickNextPage -> {
                                    navController.navigate("nextPage")
                                }
                            }
                        }

                        val shouldGoNextPageForStateFlow =
                            viewModel.uiStateFlow.collectAsStateWithLifecycle(context = Dispatchers.Main.immediate)
                        LaunchedEffect(shouldGoNextPageForStateFlow.value.shouldGoNextPageForStateFlow) {
                            if (shouldGoNextPageForStateFlow.value.shouldGoNextPageForStateFlow) {
                                viewModel.finishTransitionForStateFlow()
                                navController.navigate("nextPage")
                            }
                        }

                        val shouldGoNextPageState = viewModel.uiState.shouldGoNextPageForState
                        LaunchedEffect(shouldGoNextPageState) {
                            if (shouldGoNextPageState) {
                                viewModel.finishTransitionForState()
                                navController.navigate("nextPage")
                            }
                        }

                        FirstPage(
                            viewModel.uiState,
                            shouldGoNextPageForStateFlow.value,
                            viewModel::onClickNextPageByChannel,
                            viewModel::onClickNextPageBySharedFlow,
                            viewModel::onClickNextPageByStateFlow,
                            viewModel::onClickNextPageByState,
                        )
                    }
                    composable("nextPage") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Next Page")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FirstPage(
        uiStateByState: MainState,
        uiStateByStateFlow: MainState,
        onClickNextPageByChannel: () -> Unit,
        onClickNextPageBySharedFlow: () -> Unit,
        onClickNextPageByStateFlow: () -> Unit,
        onClickNextPageByState: () -> Unit,
    ) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
            ) {
                Button(onClick = { onClickNextPageByChannel() }) {
                    Text(text = "By Channel")
                }

                Button(onClick = { onClickNextPageBySharedFlow() }) {
                    Text(text = "By SharedFlow")
                }

                Button(onClick = { onClickNextPageByStateFlow() }) {
                    Text(text = "By StateFlow")
                }

                Button(onClick = { onClickNextPageByState() }) {
                    Text(text = "By State")
                }
            }
            if (uiStateByState.isLoading) {
                Indicator()
            }
            if (uiStateByStateFlow.isLoading) {
                Indicator()
            }
        }
    }

    @Composable
    private fun Indicator() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}

