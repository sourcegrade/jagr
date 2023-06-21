package org.anvilpowered.anvil.ui

import mui.material.Typography
import org.anvilpowered.anvil.ui.page.courses.CourseId
import org.anvilpowered.anvil.ui.page.courses.CoursesCreate
import org.anvilpowered.anvil.ui.page.courses.CoursesDetail
import org.anvilpowered.anvil.ui.page.courses.CoursesHome
import react.FC
import react.Props
import react.create
import react.router.Navigate
import react.router.Route
import react.router.Routes

val Routing = FC<Props> {


    Routes {
        Route {
            path = "/courses"
            element = CoursesHome.create()
        }
        Route {
            path = "/courses/create"
            element = CoursesCreate.create()
        }
        Route {
            path = "/courses/:$CourseId"
            element = CoursesDetail.create()
        }
        Route {
            path = "/"
            element = Navigate.create {
                to = "/courses"
            }
        }
        Route {
            path = "*"
            element = Typography.create { +"404 page not found" }
        }
    }
}
