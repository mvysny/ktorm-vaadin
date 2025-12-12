package testapp

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v23.footer
import com.github.mvysny.karibudsl.v23.openConfirmDialog
import com.github.mvysny.karibudsl.v23.setCloseOnCancel
import com.github.mvysny.karibudsl.v23.setConfirmIsDanger
import com.github.mvysny.kaributools.setDanger
import com.github.mvysny.kaributools.setPrimary
import com.github.mvysny.ktormvaadin.ActiveEntity
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.data.binder.Binder

/**
 * A form with components bound via [binder]. The form is shown in [EntityEditDialog].
 */
interface HasBinder<E> {
    val binder: Binder<E>
}

/**
 * Writes changes [toEntity] if all validation passes, and returns true.
 * If there is a validation error, nothing is written, the user is notified
 * by an error notification and false is returned.
 */
fun <E> HasBinder<E>.save(toEntity: E): Boolean {
    if (binder.validate().isOk && binder.writeBeanIfValid(toEntity)) {
        return true
    } else {
        showErrorNotification("There are errors in the form")
        return false
    }
}

/**
 * Turns the form read-only.
 */
fun HasBinder<*>.readOnly() {
    binder.setReadOnly(true)
}

/**
 * Starts editing [entity] in given [form]. Save button calls [ActiveEntity.save] if validation passed,
 * then it calls [onChange].
 */
class EntityEditDialog<E : ActiveEntity<E>>(val entity: E, val entityName: String, val form: HasBinder<E>, val onChange: () -> Unit) :
    Dialog() {
    val isCreating: Boolean get() = !entity.hasId

    init {
        headerTitle = "${if (isCreating) "Create" else "Edit"} $entityName"
        add(form as Component)
        form.binder.readBean(entity)
        footer {
            button("Save") {
                setPrimary()
                onClick { save() }
            }
            button("Cancel") {
                onClick { close() }
            }
            if (!isCreating) {
                button("Delete") {
                    addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY)
                    onClick { delete() }
                }
            }
        }
    }

    /**
     * Attempts to write the changes to the entity and then save entity to the database.
     */
    private fun save() {
        if (form.save(entity)) {
            entity.save()
            close()
            onChange()
        }
    }

    private fun delete() {
        val frame = this
        openConfirmDialog("Delete", "Are you sure you want to delete $entityName?") {
            setConfirmIsDanger()
            setConfirmButton("Delete") {
                entity.delete()
                frame.close()
                onChange()
            }
            setCloseOnCancel("Cancel")
        }
    }
}