package com.github.mvysny.ktormvaadin.utils

import com.github.mvysny.kaributesting.v10.KaribuConfig
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._get
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

class PopupButtonTest {
    @BeforeEach fun setup() { MockVaadin.setup() }
    @AfterEach fun teardown() { MockVaadin.tearDown() }

    @Test fun smoke() {
        val btn = PopupButton("foo")
        btn.setPopupContent(Button("Hello!"))
        UI.getCurrent().add(btn)

        _get<PopupButton>()
        _get<Button> { text = "Hello!" }
    }

    /**
     * The popup is closed client-side, by a workaround for
     * https://github.com/vaadin/vaadin-menu-bar/issues/102 - all we can assert
     * server-side is that the JS reaches the browser.
     */
    @Test fun `close() sends the closing JS to the browser`() {
        val js = mutableListOf<String>()
        val handler: (PendingJavaScriptInvocation) -> Unit = { js += it.invocation.expression }
        KaribuConfig.pendingJavascriptInvocationHandlers.add(handler)
        try {
            val btn = PopupButton("foo")
            btn.setPopupContent(Button("Hello!"))
            UI.getCurrent().add(btn)
            MockVaadin.clientRoundtrip()
            js.clear()

            btn.close()
            MockVaadin.clientRoundtrip()
            expect(true, js.toString()) { js.any { it.contains("_subMenu.close()") } }
        } finally {
            KaribuConfig.pendingJavascriptInvocationHandlers.remove(handler)
        }
    }

    @Test fun `content keeps its width and height`() {
        val btn = PopupButton("foo")
        btn.setPopupContent(VerticalLayout().apply { width = "200px"; height = "300px" })
        UI.getCurrent().add(btn)
        expect("200px") { _get<VerticalLayout>().width }
        expect("300px") { _get<VerticalLayout>().height }
    }
}
