package org.anvilpowered.anvil.ui.component

import csstype.AlignItems
import csstype.BoxSizing
import csstype.Display
import csstype.JustifyContent
import csstype.em
import csstype.px
import mui.icons.material.Dashboard
import mui.icons.material.Storage
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
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML
import react.router.useNavigate

external interface SectionTypographyProps : Props {
    var text: String
}

val SectionTypography = FC<SectionTypographyProps> {
    Typography {
        sx { paddingLeft = 1.em }
        variant = TypographyVariant.overline
        noWrap = true
        +it.text
    }
}

const val drawerWidth = 240

val Sidebar = FC<Props> {

    val nav = useNavigate()

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
            ReactHTML.img {
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
                ListItemIcon { Dashboard() }
                ListItemText { primary = ReactNode("Dashboard") }
            }
            MenuItem {
                onClick = { nav("/servers") }
                ListItemIcon { Storage() }
                ListItemText { primary = ReactNode("Servers") }
            }
        }
    }
}
