package com.yahoo.ycsb.penalty;

public abstract class Penalty
{
    public abstract double getTotalPenalty();
    
    public abstract double getQoSPenalty();
    
    public abstract double getQoDPenalty();
    
    public abstract double getTardiness();
    
    public abstract double getStaleness();
    
}
