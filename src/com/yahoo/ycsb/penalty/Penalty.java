package com.yahoo.ycsb.penalty;

import java.lang.reflect.Field;

import org.apache.cassandra.db.SchedulerParameter;

public class Penalty extends SchedulerParameter 
{   
    public Penalty(String str) {
        super(str);
    }
    
    public double getTotalPenalty()
    {
        return getQoSPenalty()+getQoDPenalty();
    }
    
    
    public double getQoSPenalty()
    {
        return QoS_preference*query_weight*getTardiness();
    }
    
    public double getQoDPenalty()
    {
        return (1-QoS_preference)*query_weight*getStaleness();
    }
    
    public double getTardiness()
    {
        long tardiness = local_finished_time- tardiness_deadline;
        return (tardiness>0)?tardiness:0;
    }
    
    public double getStaleness()
    {
        if (staleness_deadline==Long.MAX_VALUE)
        {
            return 0;
        }
        
        long staleness = local_finished_time-staleness_deadline;
        return (staleness>0)?staleness:0;
    }
    
    public String toprint()
    {
        StringBuffer sb = new StringBuffer();
        
        Field[] fields = this.getClass().getFields();

        for (int i = 0; i < fields.length; i++)
        {
            String varName = fields[i].getName();
            
            sb.append(varName);
            
            try
            {
                Object o = fields[i].get(this);

                sb.append("=");
                sb.append(o.toString());
                sb.append(",");

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        return sb.toString();
    }
    
}
