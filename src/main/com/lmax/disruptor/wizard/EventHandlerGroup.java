/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor.wizard;

import com.lmax.disruptor.AbstractEvent;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;

/** A group of eventprocessors set up via the {@link DisruptorWizard}.
 *
 * @param <T> the type of entry used by the eventprocessors.
 */
public class EventHandlerGroup<T extends AbstractEvent>
{
    private final DisruptorWizard<T> disruptorWizard;
    private final EventProcessor[] eventprocessors;

    EventHandlerGroup(final DisruptorWizard<T> disruptorWizard, final EventProcessor[] eventprocessors)
    {
        this.disruptorWizard = disruptorWizard;
        this.eventprocessors = eventprocessors;
    }

    /** Set up batch handlers to consume events from the ring buffer. These handlers will only process events
     *  after every eventprocessor in this group has processed the event.
     *
     *  <p>This method is generally used as part of a chain. For example if the handler <code>A</code> must
     *  process events before handler <code>B</code>:</p>
     *
     *  <pre><code>dw.consumeWith(A).then(B);</code></pre>
     *
     * @param handlers the batch handlers that will consume events.
     * @return a {@link EventHandlerGroup} that can be used to set up a eventprocessor barrier over the created eventprocessors.
     */
    public EventHandlerGroup<T> then(final EventHandler<T>... handlers)
    {
        return consumeWith(handlers);
    }

    /** Set up batch handlers to consume events from the ring buffer. These handlers will only process events
     *  after every eventprocessor in this group has processed the event.
     *
     *  <p>This method is generally used as part of a chain. For example if the handler <code>A</code> must
     *  process events before handler <code>B</code>:</p>
     *
     *  <pre><code>dw.after(A).consumeWith(B);</code></pre>
     *
     * @param handlers the batch handlers that will consume events.
     * @return a {@link EventHandlerGroup} that can be used to set up a eventprocessor barrier over the created eventprocessors.
     */
    public EventHandlerGroup<T> consumeWith(final EventHandler<T>... handlers)
    {
        return disruptorWizard.createEventProcessors(eventprocessors, handlers);
    }
}
