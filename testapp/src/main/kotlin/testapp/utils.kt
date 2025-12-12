package testapp

import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant

fun showErrorNotification(message: String) {
    Notification.show(message).apply {
        isAssertive = true
        addThemeVariants(NotificationVariant.LUMO_PRIMARY, NotificationVariant.LUMO_ERROR)
        position = Notification.Position.TOP_CENTER
    }
}