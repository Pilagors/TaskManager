package fr.monfort.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier

import fr.monfort.taskmanager.data.repository.InMemoryTaskRepository
import fr.monfort.taskmanager.domain.usecase.CreateTask
import fr.monfort.taskmanager.ui.screen.HomeScreen
import fr.monfort.taskmanager.ui.theme.TaskManagerTheme

class MainActivity : ComponentActivity() {
    private val repository = InMemoryTaskRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTests()

        enableEdgeToEdge()
        setContent {
            TaskManagerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .imePadding()
                ) {
                    HomeScreen(repository)
                }
            }
        }
    }

    private fun initTests() {
        val idParent1 = CreateTask(repository).execute("Faire ses devoirs")
        CreateTask(repository).execute("Déployer sur la main", idParent1)
        CreateTask(repository).execute("Mettre plein de données sur la main", idParent1)
        val idParent3 = CreateTask(repository).execute("Faire de l'IA (TP noté)", idParent1)
        CreateTask(repository).execute("Tout demander à chatgoat", idParent3)
        CreateTask(repository).execute("Faire un mini rapport pour être sur", idParent3)
        CreateTask(repository).execute("Finaliser le projet crypto et l'envoyer", idParent1)
        val idParent2 = CreateTask(repository).execute("Faire caca")
        CreateTask(repository).execute("Dans les toilettes", idParent2)
        CreateTask(repository).execute("Dehors", idParent2)
    }
}