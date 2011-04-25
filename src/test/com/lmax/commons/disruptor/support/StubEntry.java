package com.lmax.commons.disruptor.support;

import com.lmax.commons.disruptor.AbstractEntry;
import com.lmax.commons.disruptor.Factory;

public final class StubEntry extends AbstractEntry
{
    private int value;

    public StubEntry(int i)
    {
        this.value = i;
    }

    public void copy(StubEntry entry)
    {
        value = entry.value;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public final static Factory<StubEntry> FACTORY = new Factory<StubEntry>()
    {
        public StubEntry create()
        {
            return new StubEntry(-1);
        }
    };

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        StubEntry other = (StubEntry)obj;

        return value == other.value;
    }
}
