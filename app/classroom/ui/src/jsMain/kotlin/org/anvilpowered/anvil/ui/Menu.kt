package org.anvilpowered.anvil.ui

import csstype.AlignItems
import csstype.Display
import csstype.JustifyContent
import csstype.pct
import csstype.px
import mui.material.AppBar
import mui.material.Toolbar
import mui.system.sx
import org.anvilpowered.anvil.ui.component.Sidebar
import org.anvilpowered.anvil.ui.component.avatar.AvatarButton
import org.anvilpowered.anvil.ui.component.avatar.AvatarDrawer
import react.FC
import react.Props
import react.useState
import web.events.Event

val Menu = FC<Props> {

    var avatarDrawerOpen by useState(false)

    val handleDrawerClose: (Event, dynamic) -> Unit = { _, _ ->
        avatarDrawerOpen = false
    }

    AppBar {
        sx {
            width = 100.pct
            marginLeft = 0.px
        }
        Toolbar {
            sx {
                display = Display.flex
                gap = 16.px
                alignItems = AlignItems.center
                justifyContent = JustifyContent.end
            }
            AvatarButton {
                onClick = {
                    avatarDrawerOpen = true
                }
            }
        }
    }
    Sidebar()
    AvatarDrawer {
        open = avatarDrawerOpen
        onClose = handleDrawerClose
    }
}
