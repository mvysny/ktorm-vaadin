package testapp

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.Route

@Route("")
class MainRoute : KComposite() {
    val root = ui {
       verticalLayout {
           h1("ktorm-vaadin")
       }
    }
}