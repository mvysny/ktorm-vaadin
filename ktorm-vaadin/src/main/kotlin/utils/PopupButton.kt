package com.github.mvysny.ktormvaadin.utils

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.menubar.MenuBarVariant


/**
 * A popup button: a button with a caption, which, upon clicking, will show a
 * popup with contents.
 * @author mavi@vaadin.com
 */
class PopupButton(caption: String = "") : Composite<Component?>(), HasSize {
    /**
     * Internally, the button is implemented via Vaadin [MenuBar] with one root [.menuItem].
     */
    private val menu = MenuBar()

    /**
     * The single root menu item in the [.menu]. Holds the contents of the popup;
     * the contents are automatically shown when the menu item is clicked.
     */
    private val menuItem: MenuItem = menu.addItem("")

    var caption: String
        /**
         * Returns the current caption shown on the button.
         * @return the current caption, empty by default.
         */
        get() = menuItem.text
        /**
         * The caption, shown on the button.
         * @param caption the new caption.
         */
        set(caption) {
            menuItem.setText(caption)
        }

    init {
        this.caption = caption
    }

    override fun initContent(): Component = menu

    /**
     * Sets the popup contents, removing any previously set content.
     * @param content the new content to set, not null.
     */
    fun setPopupContent(content: Component) {
        menuItem.subMenu.removeAll()
        menuItem.subMenu.addComponent(content)
    }

    /**
     * Adds theme variants to the component.
     * @param variants theme variants to add
     */
    fun addThemeVariants(vararg variants: MenuBarVariant?) {
        menu.addThemeVariants(*variants)
    }

    /**
     * Closes the popup button. Does nothing if the popup button is already closed.
     */
    fun close() {
        // workaround for https://github.com/vaadin/vaadin-menu-bar/issues/102
        menu.getElement().executeJs("this._subMenu.close()")
    }
}
