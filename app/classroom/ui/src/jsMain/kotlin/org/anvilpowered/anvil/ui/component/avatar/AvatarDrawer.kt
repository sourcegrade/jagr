/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anvilpowered.anvil.ui.component.avatar

import csstype.AlignItems
import csstype.BoxSizing
import csstype.Display
import csstype.JustifyContent
import csstype.px
import mui.material.Box
import mui.material.Divider
import mui.material.Drawer
import mui.material.DrawerAnchor
import mui.material.DrawerVariant
import mui.material.ListItemText
import mui.material.MenuItem
import mui.material.MenuList
import mui.material.Typography
import mui.system.sx
import org.anvilpowered.anvil.ui.component.SectionTypography
import org.anvilpowered.anvil.ui.component.drawerWidth
import react.FC
import react.Props
import react.ReactNode
import react.router.useNavigate
import web.events.Event

external interface AvatarDrawerProps : Props {
    var open: Boolean
    var onClose: (Event, dynamic) -> Unit
}

val AvatarDrawer = FC<AvatarDrawerProps> { props ->

    val nav = useNavigate()

    Drawer {
        sx {
            width = 250.px
            "& .MuiDrawer-paper" {
                width = drawerWidth.px
                boxSizing = BoxSizing.borderBox
            }
        }
        open = props.open
        onClose = props.onClose
        variant = DrawerVariant.temporary
        anchor = DrawerAnchor.right
        Box {
            sx {
                display = Display.flex
                gap = 16.px
                padding = 16.px
                alignItems = AlignItems.center
                justifyContent = JustifyContent.start
            }
            AvatarButton()
            Typography {
                +"John Smith"
            }
        }
        Divider()
        SectionTypography {
            text = "Profile"
        }
        MenuList {
            MenuItem {
                onClick = { nav("/dashboard") }
                ListItemText { primary = ReactNode("Profile") }
            }
        }
    }
}
