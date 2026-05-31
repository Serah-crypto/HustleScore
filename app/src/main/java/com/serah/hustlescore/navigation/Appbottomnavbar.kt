package com.serah.hustlescore.navigation



import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.serah.hustlescore.ui.theme.ThemeViewModel

@Composable
fun AppBottomNavBar(
    navController: NavController,
    items: List<BottomNavItem>,
    themeViewModel: ThemeViewModel
)

 {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
     val isDarkMode by themeViewModel.isDarkMode.collectAsState()

     NavigationBar(
         containerColor = if (isDarkMode)
             Color(0xFF1E1E1E)
         else
             Color.White
     ) {
         items.forEach { item ->
             NavigationBarItem(
                 selected = currentRoute == item.route,
                 onClick = {
                     if (currentRoute != item.route) {
                         navController.navigate(item.route) {
                             popUpTo(navController.graph.startDestinationId) {
                                 saveState = true
                             }
                             launchSingleTop = true
                             restoreState = true
                         }
                     }
                 },
                 icon = {
                     Icon(
                         imageVector = item.icon,
                         contentDescription = item.label
                     )
                 },
                 label = {
                     Text(item.label)
                 }
             )
         }
     }


}