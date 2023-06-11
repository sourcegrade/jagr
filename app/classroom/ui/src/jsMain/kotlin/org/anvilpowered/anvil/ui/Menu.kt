package org.anvilpowered.anvil.ui

import csstype.AlignItems
import csstype.BoxSizing
import csstype.Display
import csstype.JustifyContent
import csstype.pct
import csstype.px
import mui.material.AppBar
import mui.material.Avatar
import mui.material.Drawer
import mui.material.DrawerAnchor
import mui.material.DrawerVariant
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.MenuItem
import mui.material.MenuList
import mui.material.Toolbar
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.anvilpowered.anvil.ui.component.SectionTypography
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.img
import react.router.useNavigate
import mui.icons.material.Dashboard as DashboardIcon
import mui.icons.material.Folder as FolderIcon
import mui.icons.material.Storage as StorageIcon

val drawerWidth = 240;

val Menu = FC<Props> {

    val nav = useNavigate()

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
            Avatar {
                FolderIcon()
                sx {
                    width = 32.px
                    height = 32.px
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
