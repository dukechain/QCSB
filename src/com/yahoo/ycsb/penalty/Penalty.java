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
        return 0d;
    }
    
    
    public double getQoSPenalty()
    {
        return 0d;
    }
    
    public double getQoDPenalty()
    {
        return 0d;
    }
    
    public double getTardiness()
    {
        return 0d;
    }
    
    public double getStaleness()
    {
        return 0d;
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
