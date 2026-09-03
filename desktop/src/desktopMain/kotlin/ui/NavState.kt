package ui

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    object Search : Screen()
    object Library : Screen()
    object Favorites : Screen()
    object Settings : Screen()
    object Profile : Screen()
}
