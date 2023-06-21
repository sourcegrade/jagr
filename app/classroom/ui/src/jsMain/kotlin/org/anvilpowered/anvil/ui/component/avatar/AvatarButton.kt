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

import csstype.px
import mui.icons.material.Person
import mui.material.Avatar
import mui.system.sx
import react.FC
import react.Props
import react.dom.events.MouseEventHandler
import web.html.HTMLElement

external interface AvatarButtonProps : Props {
    var onClick: MouseEventHandler<HTMLElement>
}

val AvatarButton = FC<AvatarButtonProps> { props ->
    Avatar {
        Person()
        onClick = props.onClick
        sx {
            width = 32.px
            height = 32.px
        }
    }
}
