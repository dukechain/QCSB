package com.yahoo.ycsb.penalty;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;


public class SchedulerParameter extends Penalty
{

    //user specification
    public long tardiness_deadline;
    public long staleness_deadline;
    
    public double QoS_preference;
    public double query_weight;
    
    //system behaviour
    public boolean isInstalled = true;
    
    //important timestamp
    public long issue_time = -1;
    public long arrival_time = -1;
    
    public long local_start_time = -1;
    public long local_finished_time = -1;
    
    //execution time estimation
    public long estimated_QC_k = -1;
    public long estimated_UC_k = -1;
    
    //the actual execution time
    public long actual_QC_k = -1;
    public long actual_UC_k = -1;
    
    private static final String Token = ",";
    
  
    @Override
    public double getTotalPenalty()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getQoSPenalty()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getQoDPenalty()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getTardiness()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getStaleness()
    {
        // TODO Auto-generated method stub
        return 0;
    }

  
    
    public SchedulerParameter(String str) {
        StringTokenizer tokenizer = new StringTokenizer(str, Token);
        
        tardiness_deadline = Long.parseLong(tokenizer.nextToken());
        staleness_deadline = Long.parseLong(tokenizer.nextToken());
        
        QoS_preference = Double.parseDouble(tokenizer.nextToken());
        query_weight = Double.parseDouble(tokenizer.nextToken());
        
        isInstalled = Boolean.parseBoolean(tokenizer.nextToken());
        
        issue_time = Long.parseLong(tokenizer.nextToken());
        arrival_time = Long.parseLong(tokenizer.nextToken());
        
        local_start_time = Long.parseLong(tokenizer.nextToken());
        local_finished_time = Long.parseLong(tokenizer.nextToken());
        
        estimated_QC_k = Long.parseLong(tokenizer.nextToken());
        estimated_UC_k = Long.parseLong(tokenizer.nextToken());
        
        actual_QC_k = Long.parseLong(tokenizer.nextToken());
        actual_UC_k = Long.parseLong(tokenizer.nextToken());      
    }
    
    public SchedulerParameter(ByteBuffer bb) {
        this(toString(bb));
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(tardiness_deadline);
        sb.append(Token);
        sb.append(staleness_deadline);
        sb.append(Token);
        
        sb.append(QoS_preference);
        sb.append(Token);
        sb.append(query_weight);
        sb.append(Token);
        
        sb.append(isInstalled);
        sb.append(Token);
        
        sb.append(issue_time);
        sb.append(Token);
        sb.append(arrival_time);
        sb.append(Token);
        
        sb.append(local_start_time);
        sb.append(Token);
        sb.append(local_finished_time);
        sb.append(Token);
        
        sb.append(estimated_QC_k);
        sb.append(Token);
        sb.append(estimated_UC_k);
        sb.append(Token);
        
        sb.append(actual_QC_k);
        sb.append(Token);
        sb.append(actual_UC_k);
        
        return sb.toString();
    }
    
    public ByteBuffer toByteBuffer()
    {
        return toByteBuffer(toString());
    }
    
    public static String toString(ByteBuffer buffer) 
    {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        
        String str = null;
        try
        {
            str = new String(bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        
        return str;
     }
    
    public static ByteBuffer toByteBuffer(String str) 
    {
        byte [] array = null;
        try
        {
            array = str.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        
        return ByteBuffer.wrap(array);
    }


}
