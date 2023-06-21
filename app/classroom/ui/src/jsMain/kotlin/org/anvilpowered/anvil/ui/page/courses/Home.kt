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

package org.anvilpowered.anvil.ui.page.courses

import csstype.Display
import csstype.number
import csstype.px
import domain.course.Course
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.anvilpowered.anvil.ui.component.SearchBox
import org.sourcegrade.jagr.api.WebJagrApi
import org.sourcegrade.jagr.api.course.CourseDto
import react.FC
import react.Props
import react.router.useNavigate
import react.useState
import mui.icons.material.Add as AddIcon

val CoursesHome = FC<Props> {

    /**
     * The courses displayed on this page.
     */
    var (courses, setCourses) = useState<List<CourseDto.PaginationElement>>(listOf())

    CoroutineScope(Dispatchers.Default).run {
        launch {
            WebJagrApi.run {
                courses = Course.paginate(
                    page = 0,
                    pageSize = 10,
                    sortBy = CourseDto.PaginationElement::name,
                    ascending = false,
                )
            }
        }
    }

    val nav = useNavigate()

    Toolbar {
        sx {
            display = Display.flex
            gap = 16.px
        }
        Typography {
            sx {
                flexGrow = number(1.0)
            }
            variant = TypographyVariant.overline
            +"Servers"
        }
        SearchBox()
        IconButton {
            onClick = { nav("/servers/create") }
            AddIcon()
        }
    }
    TableContainer {
        component = Paper
        Table {
            TableHead {
                TableRow {
                    TableCell { +"Server Name" }
                    TableCell { align = TableCellAlign.right; +"Players" }
                }
            }
            TableBody {
                courses.forEach { course ->
                    TableRow {
                        TableCell { +course.id }
                        TableCell { +course.name }
                    }
                }
            }
        }
    }
}
