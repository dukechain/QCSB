package com.yahoo.ycsb.recordlogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.yahoo.ycsb.measurements.exporter.MeasurementsExporter;

public class OneRecordLogPerline extends OneRecordLog
{
    List<String> records;

   
    public OneRecordLogPerline(String _name, Properties _props)
    {
        super(_name);
        
        records = new ArrayList<String>();
    }
    
    @Override
    public void recordlog(String content)
    {
       records.add(content);
        
    }

    @Override
    public void exportRecordLogs(MeasurementsExporter exporter) throws IOException
    {
        exporter.write(getName(), "size", records.size());
        
        for (String str : records)
        {
            exporter.write(getName(), str, Integer.MIN_VALUE);
        }
        
    }

}
