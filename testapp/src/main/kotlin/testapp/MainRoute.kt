package testapp

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route("")
class MainRoute : VerticalLayout() {
    init {
        add(H1("Hello!"))
    }
}