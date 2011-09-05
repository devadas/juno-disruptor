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
package com.lmax.disruptor;

/**
 * Coordinator for claiming sequences for access to a data structure while tracking dependent {@link Sequence}s
 */
public class Sequencer
{
    /** Set to -1 as sequence starting point */
    public static final long INITIAL_CURSOR_VALUE = -1L;

    private final Sequence cursor = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private Sequence[] sequencesToTrack;

    private final ClaimStrategy claimStrategy;
    private final WaitStrategy waitStrategy;

    private final int bufferSize;

    /**
     * Construct a Sequencer with the selected strategies.
     *
     * @param claimStrategy for those claiming sequences.
     * @param waitStrategy for those waiting on sequences.
     * @param bufferSize over which sequences are valid.
     */
    public Sequencer(final ClaimStrategy claimStrategy,
                     final WaitStrategy waitStrategy,
                     final int bufferSize)
    {
        this.claimStrategy = claimStrategy;
        this.waitStrategy = waitStrategy;
        this.bufferSize = bufferSize;
    }

    /**
     * Set the sequences that will be tracked to prevent the buffer wrapping.
     *
     * This method must be called prior to claiming events in the RingBuffer otherwise
     * a NullPointerException will be thrown.
     *
     * @param sequences to be tracked.
     */
    public void setTrackedSequences(final Sequence... sequences)
    {
        this.sequencesToTrack = sequences;
    }

    /**
     * Create a {@link SequenceBarrier} that gates on the the cursor and a list of {@link Sequence}s
     *
     * @param sequencesToTrack this barrier will track
     * @return the barrier gated as required
     */
    public SequenceBarrier newSequenceBarrier(final Sequence... sequencesToTrack)
    {
        return new ProcessingSequenceBarrier(waitStrategy, cursor, sequencesToTrack);
    }


    /**
     * The capacity of the data structure to hold entries.
     *
     * @return the size of the RingBuffer.
     */
    public int getBufferSize()
    {
        return bufferSize;
    }

    /**
     * Get the value of the cursor indicating the published sequence.
     *
     * @return value of the cursor for events that have been published.
     */
    public long getCursor()
    {
        return cursor.get();
    }

    /**
     * Claim the next event in sequence for publishing to the {@link RingBuffer}
     *
     * @return the claimed sequence
     */
    public long nextSequence()
    {
        final long sequence = claimStrategy.incrementAndGet();
        claimStrategy.ensureSequencesAreInRange(sequence, sequencesToTrack);
        return sequence;
    }

    /**
     * Publish an event and make it visible to {@link EventProcessor}s
     * @param sequence to be published
     */
    public void publish(final long sequence)
    {
        publish(sequence, 1);
    }

    /**
     * Claim the next batch sequence numbers for publishing.
     *
     * @param sequenceBatch to be updated for the batch range.
     * @return the updated sequenceBatch.
     */
    public SequenceBatch nextSequenceBatch(final SequenceBatch sequenceBatch)
    {
        final int batchSize = sequenceBatch.getSize();
        if (batchSize > bufferSize)
        {
            final String msg = "Batch size " + batchSize + " is greater than buffer size of " + bufferSize;
            throw new IllegalArgumentException(msg);
        }

        final long sequence = claimStrategy.incrementAndGet(batchSize);
        sequenceBatch.setEnd(sequence);
        claimStrategy.ensureSequencesAreInRange(sequence, sequencesToTrack);
        return sequenceBatch;
    }

    /**
     * Publish the batch of events in sequence.
     *
     * @param sequenceBatch to be published.
     */
    public void publish(final SequenceBatch sequenceBatch)
    {
        publish(sequenceBatch.getEnd(), sequenceBatch.getSize());
    }

    /**
     * Claim a specific sequence when only one publisher is involved.
     *
     * @param sequence to be claimed.
     */
    public void claimSequence(final long sequence)
    {
        claimStrategy.ensureSequencesAreInRange(sequence, sequencesToTrack);
    }

    /**
     * Force the publication of a cursor sequence.
     *
     * Only use this method when forcing a sequence and you are sure only one publisher exists.
     * This will cause the cursor to advance to this sequence.
     *
     * @param sequence which is to be forced for publication.
     */
    public void forcePublish(final long sequence)
    {
        claimStrategy.setSequence(sequence);
        cursor.set(sequence);
        waitStrategy.signalAllWhenBlocking();
    }

    private void publish(final long sequence, final long batchSize)
    {
        claimStrategy.serialisePublishing(cursor, sequence, batchSize);
        cursor.set(sequence);
        waitStrategy.signalAllWhenBlocking();
    }
}
