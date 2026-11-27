package com.example.starter.base;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Checks server push over WebSocket: a background thread updates the counter
 * every second.
 */
@Route("push")
public class PushView extends VerticalLayout {

    private final Span counter = new Span("Push counter: 0");
    private final Span transport = new Span();
    private final AtomicInteger ticks = new AtomicInteger();
    private ScheduledExecutorService executor;

    public PushView() {
        counter.setId("counter");
        transport.setId("transport");
        add(counter, transport);
    }

    @Override
    protected void onAttach(AttachEvent event) {
        UI ui = event.getUI();
        transport.setText("Transport: "
                + ui.getPushConfiguration().getTransport());
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> ui.access(() -> counter
                .setText("Push counter: " + ticks.incrementAndGet())), 1, 1,
                TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent event) {
        executor.shutdownNow();
    }
}
