package com.dktechhub.mnnit.ee.statussaver;

import androidx.annotation.NonNull;

public class Pair {
    public String val;
    public int id;
    public Pair(String val,int id)
    {
        this.val=val;
        this.id=id;
    }

    @NonNull
    @Override
    public String toString() {
        return val+" ("+id+")";
    }
}
