package com.yahoo.ycsb.recordlogs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import com.yahoo.ycsb.measurements.exporter.MeasurementsExporter;
import com.yammer.metrics.util.PercentGauge;

public class RecordLogs
{
    private static final String RECORDLOG_TYPE = "recordlogtype";

    private static final String RECORDLOG_TYPE_DEFAULT = "perline";

    static RecordLogs singleton=null;
    
    static Properties measurementproperties=null;
    
    public static void setProperties(Properties props)
    {
        measurementproperties=props;
    }

      /**
       * Return the singleton Measurements object.
       */
    public synchronized static RecordLogs getRecordLogs()
    {
        if (singleton==null)
        {
            singleton=new RecordLogs(measurementproperties);
        }
        return singleton;
    }

    HashMap<String,OneRecordLog> data;
    boolean perline=true;

    private Properties _props;
    
      /**
       * Create a new object with the specified properties.
       */
    public RecordLogs(Properties props)
    {
        data=new HashMap<String,OneRecordLog>();
        
        _props=props;
        
        String testString = _props.getProperty(RECORDLOG_TYPE, RECORDLOG_TYPE_DEFAULT);
        
        if (testString.compareTo("perline")==0)
        {
            perline =true;
        }
        else
        {
            perline =false;
        }
    }
    
    OneRecordLog constructOneRecordLog(String name)
    {
        if (perline)
        {
            return new OneRecordLogPerline(name,_props);
        }
        else
        {
           // return new OneMeasurementTimeSeries(name,_props);
        }
        
        return new OneRecordLogPerline(name,_props);
    }

      /**
       * Report a single value of a single metric. E.g. for read latency, operation="READ" and latency is the measured value.
       */
    public synchronized void recordlog(String operation, String content)
    {
        if (!data.containsKey(operation))
        {
            synchronized(this)
            {
                if (!data.containsKey(operation))
                {
                    data.put(operation, constructOneRecordLog(operation));
                }
            }
        }
        try
        {
            data.get(operation).recordlog(content);
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e)
        {
            System.out.println("ERROR: java.lang.ArrayIndexOutOfBoundsException - ignoring and continuing");
            e.printStackTrace();
            e.printStackTrace(System.out);
        }
    }

      /**
       * Report a return code for a single DB operaiton.
       */
    /*public void reportReturnCode(String operation, int code)
    {
        if (!data.containsKey(operation))
        {
            synchronized(this)
            {
                if (!data.containsKey(operation))
                {
                    data.put(operation,constructOneMeasurement(operation));
                }
            }
        }
        data.get(operation).reportReturnCode(code);
    }*/
    
  /**
   * Export the current measurements to a suitable format.
   * 
   * @param exporter Exporter representing the type of format to write to.
   * @throws IOException Thrown if the export failed.
   */
  public void exportRecordLogs(MeasurementsExporter exporter) throws IOException
  {
    for (OneRecordLog recordlog : data.values())
    {
      recordlog.exportRecordLogs(exporter);
    }
  }
    
      /**
       * Return a one line summary of the measurements.
       */
   /* public String getSummary()
    {
        String ret="";
        for (OneMeasurement m : data.values())
        {
            ret+=m.getSummary()+" ";
        }
        
        return ret;
    }*/
}
