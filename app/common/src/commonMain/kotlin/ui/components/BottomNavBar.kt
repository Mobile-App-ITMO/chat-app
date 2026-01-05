package io.ktor.chat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.ui.screens.home.HomeTab
import io.ktor.chat.ui.theme.Space

@Composable
fun BottomNavBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                tab = HomeTab.Home,
                isSelected = selectedTab == HomeTab.Home,
                onClick = { onTabSelected(HomeTab.Home) }
            )

            NavigationBarItem(
                tab = HomeTab.Friends,
                isSelected = selectedTab == HomeTab.Friends,
                onClick = { onTabSelected(HomeTab.Friends) }
            )

            NavigationBarItem(
                tab = HomeTab.Settings,
                isSelected = selectedTab == HomeTab.Settings,
                onClick = { onTabSelected(HomeTab.Settings) }
            )
        }
    }
}

@Composable
fun NavigationBarItem(
    tab: HomeTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
            contentDescription = tab.title,
            modifier = Modifier.size(Space.xl),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Space.xs))

        Text(
            text = tab.title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}