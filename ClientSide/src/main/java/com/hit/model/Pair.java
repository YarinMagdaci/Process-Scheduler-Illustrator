package com.hit.model;

public class Pair {
    int m_Index;
    int m_TimeNeeded;

    public Pair(int i_Index, int i_TimeNeeded)
    {
        this.setIndex(i_Index);
        this.setTimeNeeded(i_TimeNeeded);
    }
    public void setIndex(int i_Index){this.m_Index = i_Index;}
    public void setTimeNeeded(int i_TimeNeeded){this.m_TimeNeeded = i_TimeNeeded;}

    public int getIndex(){return this.m_Index;}
    public int getTimeNeeded(){return this.m_TimeNeeded;}


}
