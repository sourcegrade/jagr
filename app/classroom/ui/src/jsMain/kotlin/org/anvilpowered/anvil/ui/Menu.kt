package org.anvilpowered.anvil.ui

import csstype.*
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.anvilpowered.anvil.ui.component.SectionTypography
import react.*
import react.dom.events.MouseEventHandler
import react.dom.findDOMNode
import react.dom.html.ReactHTML.img
import react.router.useNavigate
import web.dom.document
import web.html.HTMLElement
import mui.icons.material.Dashboard as DashboardIcon
import mui.icons.material.Person as PersonIcon
import mui.icons.material.Storage as StorageIcon

val drawerWidth = 240;

val Menu = FC<Props> {

    val nav = useNavigate()

    var anchorElement by useState<HTMLElement>()

    val handleClick: MouseEventHandler<HTMLElement> = { event ->
        event.preventDefault()
        anchorElement = event.currentTarget
    }

    val handleClose: MouseEventHandler<HTMLElement> = { event ->
        event.preventDefault()
        anchorElement = null
    }

    val fakeElement = document.createElement("div")

    AppBar {
        sx {
//            width = 100.pct - drawerWidth.px
//            marginLeft = drawerWidth.px
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
            val avatar: ReactElement<*> = Avatar.create {
                PersonIcon()
                onClick = handleClick
                sx {
                    width = 32.px
                    height = 32.px
                }
            }
            +avatar
            Menu {
                anchorEl = { anchorElement ?: fakeElement }
                open = anchorElement != null
                onClose = handleClose
                onClick = handleClose
                MenuItem {
                    onClick = { nav("/account") }
                    ListItemIcon { StorageIcon() }
                    ListItemText { primary = ReactNode("Account") }
                }
            }
        }
    }
    Drawer {
        sx {
            width = drawerWidth.px
            "& .MuiDrawer-paper" {
                width = drawerWidth.px
                boxSizing = BoxSizing.borderBox
            }
        }
        variant = DrawerVariant.permanent
        anchor = DrawerAnchor.left
        Toolbar {
            sx {
                display = Display.flex
                gap = 16.px
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
            }
            img {
                src = "/logo-square.svg"
                height = 38.0
            }
            Typography {
                variant = TypographyVariant.h6
                +"Classroom"
            }
        }
        SectionTypography {
            text = "Home"
        }
        MenuList {
            MenuItem {
                onClick = { nav("/dashboard") }
                ListItemIcon { DashboardIcon() }
                ListItemText { primary = ReactNode("Dashboard") }
            }
            MenuItem {
                onClick = { nav("/servers") }
                ListItemIcon { StorageIcon() }
                ListItemText { primary = ReactNode("Servers") }
            }
        }
    }
}
